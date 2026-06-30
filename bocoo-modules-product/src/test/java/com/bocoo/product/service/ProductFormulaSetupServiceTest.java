package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductFormulaMaterialBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionMaterialBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionValueBo;
import com.bocoo.product.domain.bo.ProductFormulaRestrictionBo;
import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.bo.ProductFormulaUsageRuleBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductUnit;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaMaterialMapper;
import com.bocoo.product.mapper.ProductFormulaOptionMapper;
import com.bocoo.product.mapper.ProductFormulaOptionMaterialMapper;
import com.bocoo.product.mapper.ProductFormulaOptionValueMapper;
import com.bocoo.product.mapper.ProductFormulaRestrictionMapper;
import com.bocoo.product.mapper.ProductFormulaUsageRuleMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductUnitMapper;
import com.bocoo.product.service.impl.ProductFormulaMaterialSnapshotResolver;
import com.bocoo.product.service.impl.ProductFormulaRestrictionNormalizer;
import com.bocoo.product.service.impl.ProductFormulaSetupNormalizer;
import com.bocoo.product.service.impl.ProductFormulaSetupReader;
import com.bocoo.product.service.impl.ProductFormulaSetupServiceImpl;
import com.bocoo.product.service.impl.ProductFormulaSetupValidator;
import com.bocoo.product.service.impl.ProductFormulaSetupWriter;
import com.bocoo.product.service.impl.ProductFormulaUsageRuleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductFormulaSetupServiceTest {

    @Mock
    private ProductFormulaMapper formulaMapper;
    @Mock
    private ProductFormulaMaterialMapper materialMapper;
    @Mock
    private ProductFormulaOptionMapper optionMapper;
    @Mock
    private ProductFormulaOptionValueMapper optionValueMapper;
    @Mock
    private ProductFormulaOptionMaterialMapper optionMaterialMapper;
    @Mock
    private ProductFormulaRestrictionMapper restrictionMapper;
    @Mock
    private ProductFormulaUsageRuleMapper usageRuleMapper;
    @Mock
    private ProductMaterialMapper productMaterialMapper;
    @Mock
    private ProductUnitMapper unitMapper;
    @Mock
    private ProductChangeLogService changeLogService;

    private ProductFormulaSetupServiceImpl setupService;
    private ProductFormulaUsageRuleServiceImpl usageRuleService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        usageRuleService = new ProductFormulaUsageRuleServiceImpl(usageRuleMapper);
        ProductFormulaMaterialSnapshotResolver materialSnapshotResolver =
            new ProductFormulaMaterialSnapshotResolver(productMaterialMapper, unitMapper);
        ProductFormulaSetupReader setupReader = new ProductFormulaSetupReader(
            formulaMapper,
            materialMapper,
            optionMapper,
            optionValueMapper,
            optionMaterialMapper,
            restrictionMapper,
            productMaterialMapper,
            usageRuleService
        );
        ProductFormulaSetupValidator setupValidator = new ProductFormulaSetupValidator(usageRuleService);
        ProductFormulaRestrictionNormalizer restrictionNormalizer = new ProductFormulaRestrictionNormalizer(setupValidator);
        ProductFormulaSetupNormalizer setupNormalizer =
            new ProductFormulaSetupNormalizer(materialSnapshotResolver, restrictionNormalizer, usageRuleService);
        ProductFormulaSetupWriter setupWriter = new ProductFormulaSetupWriter(
            materialMapper,
            optionMapper,
            optionValueMapper,
            optionMaterialMapper,
            restrictionMapper,
            usageRuleService
        );
        setupService = new ProductFormulaSetupServiceImpl(
            formulaMapper,
            changeLogService,
            setupReader,
            setupValidator,
            setupNormalizer,
            setupWriter
        );
    }

    @Test
    void effectiveFormulaCannotSaveSetup() {
        when(formulaMapper.selectById(3001L)).thenReturn(formula("EFFECTIVE"));

        assertThatThrownBy(() -> setupService.saveSetup(3001L, validSetup()))
            .isInstanceOf(ServiceException.class);

        verify(materialMapper, never()).insert(any());
    }

    @Test
    void duplicateMaterialIsRejected() {
        ProductFormulaSetupBo bo = validSetup();
        bo.setMaterials(List.of(materialBo(4001L), materialBo(4001L)));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();

        assertThatThrownBy(() -> setupService.saveSetup(3001L, bo))
            .isInstanceOf(ServiceException.class);

        verify(materialMapper, never()).insert(any());
    }

    @Test
    void saveSetupWritesSeparatedSetupTablesAndResetsValidation() {
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();
        when(formulaMapper.update(any(), any())).thenReturn(1);
        doAnswer(invocation -> {
            ProductFormulaMaterial row = invocation.getArgument(0);
            row.setFormulaMaterialId(7001L);
            return 1;
        }).when(materialMapper).insert(any(ProductFormulaMaterial.class));
        doAnswer(invocation -> {
            ProductFormulaOption row = invocation.getArgument(0);
            row.setOptionId(7101L);
            return 1;
        }).when(optionMapper).insert(any(ProductFormulaOption.class));
        doAnswer(invocation -> {
            ProductFormulaOptionValue row = invocation.getArgument(0);
            row.setOptionValueId(7201L);
            return 1;
        }).when(optionValueMapper).insert(any(ProductFormulaOptionValue.class));
        when(optionMaterialMapper.insert(any(ProductFormulaOptionMaterial.class))).thenReturn(1);
        when(restrictionMapper.insert(any(ProductFormulaRestriction.class))).thenReturn(1);
        when(usageRuleMapper.insert(any(ProductFormulaUsageRule.class))).thenReturn(1);

        assertThat(setupService.saveSetup(3001L, validSetup())).isTrue();

        verify(materialMapper).insert(argThat(row ->
            Long.valueOf(3001L).equals(row.getFormulaId())
                && Long.valueOf(4001L).equals(row.getMaterialId())
                && "MAT001".equals(row.getMaterialCode())
                && "FIXED".equals(row.getUsageMode())
        ));
        verify(optionMapper).insert(argThat(row -> "FABRIC".equals(row.getOptionCode())));
        verify(optionValueMapper).insert(argThat(row -> "MAT001".equals(row.getValueCode())));
        verify(optionMaterialMapper).insert(argThat(row ->
            Long.valueOf(7001L).equals(row.getFormulaMaterialId())
                && Long.valueOf(7101L).equals(row.getOptionId())
                && Long.valueOf(7201L).equals(row.getOptionValueId())
        ));
        verify(restrictionMapper).insert(argThat(row -> "WIDTH".equals(row.getConditionType())));
        verify(usageRuleMapper).insert(argThat(row ->
            "MAT001".equals(row.getMaterialCode())
                && "DEFAULT".equals(row.getConditionType())
                && Boolean.TRUE.equals(row.getDefaultRuleFlag())
        ));
        verify(changeLogService).record(eq("FORMULA"), eq("FORMULA"), eq(3001L), eq("FORMULA_25_ZEBRA"), eq("SAVE_SETUP"), any(), any(), eq(null));
    }

    @Test
    void duplicateUsageRuleConditionIsRejected() {
        ProductFormulaSetupBo bo = validSetup();
        bo.setUsageRules(List.of(usageRuleBo(false), usageRuleBo(false)));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();

        assertThatThrownBy(() -> setupService.saveSetup(3001L, bo))
            .isInstanceOf(ServiceException.class);

        verify(usageRuleMapper, never()).insert(any());
    }

    @Test
    void fixedUsageModeOnlyAllowsOneRule() {
        ProductFormulaSetupBo bo = validSetup();
        bo.setUsageRules(List.of(fixedUsageRuleBo(), fixedUsageRuleBo()));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();

        assertThatThrownBy(() -> setupService.saveSetup(3001L, bo))
            .isInstanceOf(ServiceException.class);

        verify(usageRuleMapper, never()).insert(any());
    }

    @Test
    void formulaUsageModeRequiresDefaultRule() {
        ProductFormulaSetupBo bo = validSetup();
        bo.setUsageRules(List.of(usageRuleBo(false)));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();

        assertThatThrownBy(() -> setupService.saveSetup(3001L, bo))
            .isInstanceOf(ServiceException.class);

        verify(usageRuleMapper, never()).insert(any());
    }

    @Test
    void conditionalOptionVisibilityIsSavedWithReadableSnapshotNames() {
        ProductFormulaSetupBo bo = validSetup();
        ProductFormulaOptionBo motorModel = optionBo("MOTOR_MODEL", "电机型号");
        motorModel.setVisibilityMode("CONDITIONAL");
        motorModel.setVisibleConditionOptionCode("FABRIC");
        motorModel.setVisibleConditionValueCode("MAT001");
        bo.setOptions(List.of(optionBo(), motorModel));
        bo.setOptionValues(List.of(optionValueBo(), optionValueBo("MOTOR_MODEL", "MOTOR_A", "A款电机")));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();
        when(formulaMapper.update(any(), any())).thenReturn(1);
        doAnswer(invocation -> {
            ProductFormulaMaterial row = invocation.getArgument(0);
            row.setFormulaMaterialId(7001L);
            return 1;
        }).when(materialMapper).insert(any(ProductFormulaMaterial.class));
        doAnswer(invocation -> {
            ProductFormulaOption row = invocation.getArgument(0);
            row.setOptionId(7101L);
            return 1;
        }).when(optionMapper).insert(any(ProductFormulaOption.class));
        doAnswer(invocation -> {
            ProductFormulaOptionValue row = invocation.getArgument(0);
            row.setOptionValueId(7201L);
            return 1;
        }).when(optionValueMapper).insert(any(ProductFormulaOptionValue.class));

        assertThat(setupService.saveSetup(3001L, bo)).isTrue();

        verify(optionMapper).insert(argThat(row ->
            "MOTOR_MODEL".equals(row.getOptionCode())
                && "CONDITIONAL".equals(row.getVisibilityMode())
                && "FABRIC".equals(row.getVisibleConditionOptionCode())
                && "面料".equals(row.getVisibleConditionOptionNameCn())
                && "MAT001".equals(row.getVisibleConditionValueCode())
                && "米色斑马帘面料".equals(row.getVisibleConditionValueNameCn())
        ));
    }

    @Test
    void conditionalOptionVisibilityCannotDependOnItself() {
        ProductFormulaSetupBo bo = validSetup();
        ProductFormulaOptionBo option = optionBo();
        option.setVisibilityMode("CONDITIONAL");
        option.setVisibleConditionOptionCode("FABRIC");
        option.setVisibleConditionValueCode("MAT001");
        bo.setOptions(List.of(option));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();

        assertThatThrownBy(() -> setupService.saveSetup(3001L, bo))
            .isInstanceOf(ServiceException.class);

        verify(optionMapper, never()).insert(any());
    }

    @Test
    void conditionalOptionVisibilityRequiresValueBelongingToConditionOption() {
        ProductFormulaSetupBo bo = validSetup();
        ProductFormulaOptionBo motorModel = optionBo("MOTOR_MODEL", "电机型号");
        motorModel.setVisibilityMode("CONDITIONAL");
        motorModel.setVisibleConditionOptionCode("FABRIC");
        motorModel.setVisibleConditionValueCode("MOTOR_A");
        bo.setOptions(List.of(optionBo(), motorModel));
        bo.setOptionValues(List.of(optionValueBo(), optionValueBo("MOTOR_MODEL", "MOTOR_A", "A款电机")));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();

        assertThatThrownBy(() -> setupService.saveSetup(3001L, bo))
            .isInstanceOf(ServiceException.class);

        verify(optionMapper, never()).insert(any());
    }

    @Test
    void formulaUsageRuleWithUnknownVariableIsRejected() {
        ProductFormulaSetupBo bo = validSetup();
        ProductFormulaUsageRuleBo usageRule = usageRuleBo(true);
        usageRule.setUsageMode("FORMULA");
        usageRule.setFixedUsageQty(null);
        usageRule.setUsageFormula("width - 1");
        bo.setUsageRules(List.of(usageRule));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();

        assertThatThrownBy(() -> setupService.saveSetup(3001L, bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void restrictionConditionValueMustBelongToConditionOptionOnSave() {
        ProductFormulaSetupBo bo = validSetup();
        bo.setOptions(List.of(optionBo(), optionBo("SYSTEM", "系统")));
        bo.setOptionValues(List.of(optionValueBo(), optionValueBo("SYSTEM", "ELECTRIC", "电机")));
        ProductFormulaRestrictionBo restriction = restrictionBo();
        restriction.setConditionType("OPTION_VALUE");
        restriction.setConditionOptionCode("SYSTEM");
        restriction.setConditionValueCode("MAT001");
        bo.setRestrictions(List.of(restriction));
        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        stubMaterialSnapshot();

        assertThatThrownBy(() -> setupService.saveSetup(3001L, bo))
            .isInstanceOf(ServiceException.class);

        verify(restrictionMapper, never()).insert(any());
    }

    @Test
    void validationRejectsPersistedRestrictionConditionValueFromAnotherOption() {
        ProductFormulaRestriction restriction = restriction();
        restriction.setConditionType("OPTION_VALUE");
        restriction.setConditionOptionCode("SYSTEM");
        restriction.setConditionValueCode("MAT001");
        when(materialMapper.selectList(any())).thenReturn(List.of(formulaMaterial()));
        when(usageRuleMapper.selectList(any())).thenReturn(List.of(usageRule()));
        when(optionMapper.selectList(any())).thenReturn(List.of(option("FABRIC", "面料"), option("SYSTEM", "系统")));
        when(optionValueMapper.selectList(any())).thenReturn(List.of(
            optionValue("FABRIC", "MAT001", "米色斑马帘面料"),
            optionValue("SYSTEM", "ELECTRIC", "电机")
        ));
        when(optionMaterialMapper.selectList(any())).thenReturn(List.of());
        when(restrictionMapper.selectList(any())).thenReturn(List.of(restriction));

        assertThat(setupService.validationMessageKey(3001L))
            .isEqualTo("product.formula.restrictionConditionInvalid");
    }

    @Test
    void validateSetupPersistsFailureStatusBeforeThrowingBusinessException() throws Exception {
        Method method = ProductFormulaSetupServiceImpl.class.getMethod("validateSetup", Long.class);
        Transactional transactional = method.getAnnotation(Transactional.class);
        assertThat(transactional.noRollbackFor()).contains(ServiceException.class);

        when(formulaMapper.selectById(3001L)).thenReturn(formula("DRAFT"));
        when(materialMapper.selectList(any())).thenReturn(List.of());
        when(formulaMapper.update(any(), any())).thenReturn(1);

        assertThatThrownBy(() -> setupService.validateSetup(3001L))
            .isInstanceOf(ServiceException.class);

        verify(formulaMapper).update(any(), any());
        verify(changeLogService).record(eq("FORMULA"), eq("FORMULA"), eq(3001L), eq("FORMULA_25_ZEBRA"), eq("VALIDATE_FAIL"), any(), any(), eq(null));
    }

    @Test
    void validationRejectsNonBooleanExpressionCondition() {
        ProductFormulaUsageRule usageRule = usageRule();
        usageRule.setConditionType("EXPRESSION");
        usageRule.setConditionExpression("orderWidth");
        usageRule.setConditionText("非布尔表达式");
        usageRule.setConditionKey("EXPR:orderWidth");
        usageRule.setDefaultRuleFlag(Boolean.FALSE);

        when(materialMapper.selectList(any())).thenReturn(List.of(formulaMaterial()));
        when(usageRuleMapper.selectList(any())).thenReturn(List.of(usageRule));

        assertThat(setupService.validationMessageKey(3001L))
            .isEqualTo("product.formula.usageConditionInvalid");
    }

    private ProductFormulaSetupBo validSetup() {
        ProductFormulaSetupBo bo = new ProductFormulaSetupBo();
        bo.setMaterials(List.of(materialBo(4001L)));
        bo.setOptions(List.of(optionBo()));
        bo.setOptionValues(List.of(optionValueBo()));
        bo.setOptionMaterials(List.of(optionMaterialBo()));
        bo.setRestrictions(List.of(restrictionBo()));
        bo.setUsageRules(List.of(usageRuleBo(true)));
        return bo;
    }

    private void stubMaterialSnapshot() {
        when(productMaterialMapper.selectList(any())).thenReturn(List.of(material(4001L, "MAT001")));
        when(unitMapper.selectList(any())).thenReturn(List.of(unit()));
    }

    private ProductFormulaMaterialBo materialBo(Long materialId) {
        ProductFormulaMaterialBo bo = new ProductFormulaMaterialBo();
        bo.setMaterialId(materialId);
        bo.setUnitCode("米");
        bo.setUsageMode("FIXED");
        bo.setFixedUsageQty(BigDecimal.ONE);
        bo.setCalculationUnitCode("米");
        bo.setLossRate(new BigDecimal("5"));
        bo.setStatus("ENABLED");
        return bo;
    }

    private ProductFormulaOptionBo optionBo() {
        return optionBo("FABRIC", "面料");
    }

    private ProductFormulaOptionBo optionBo(String optionCode, String optionNameCn) {
        ProductFormulaOptionBo bo = new ProductFormulaOptionBo();
        bo.setOptionCode(optionCode);
        bo.setOptionNameCn(optionNameCn);
        bo.setSourceType("MATERIAL_POOL");
        bo.setSelectionMode("SINGLE");
        bo.setRequiredFlag(Boolean.TRUE);
        bo.setBusinessVisibleFlag(Boolean.TRUE);
        bo.setStatus("ENABLED");
        return bo;
    }

    private ProductFormulaOptionValueBo optionValueBo() {
        return optionValueBo("FABRIC", "MAT001", "米色斑马帘面料");
    }

    private ProductFormulaOptionValueBo optionValueBo(String optionCode, String valueCode, String valueNameCn) {
        ProductFormulaOptionValueBo bo = new ProductFormulaOptionValueBo();
        bo.setOptionCode(optionCode);
        bo.setValueCode(valueCode);
        bo.setValueNameCn(valueNameCn);
        bo.setStatus("ENABLED");
        return bo;
    }

    private ProductFormulaOptionMaterialBo optionMaterialBo() {
        ProductFormulaOptionMaterialBo bo = new ProductFormulaOptionMaterialBo();
        bo.setOptionCode("FABRIC");
        bo.setValueCode("MAT001");
        bo.setMaterialCode("MAT001");
        bo.setRequiredFlag(Boolean.TRUE);
        bo.setDefaultFlag(Boolean.TRUE);
        bo.setStatus("ENABLED");
        return bo;
    }

    private ProductFormulaRestrictionBo restrictionBo() {
        ProductFormulaRestrictionBo bo = new ProductFormulaRestrictionBo();
        bo.setRestrictionName("宽度过大禁用面料");
        bo.setTargetOptionCode("FABRIC");
        bo.setTargetValueCode("MAT001");
        bo.setConditionType("WIDTH");
        bo.setConditionOperator("GT");
        bo.setConditionValueNumber(new BigDecimal("120"));
        bo.setActionType("DISABLE");
        bo.setStatus("ENABLED");
        return bo;
    }

    private ProductFormulaMaterial formulaMaterial() {
        ProductFormulaMaterial material = new ProductFormulaMaterial();
        material.setFormulaMaterialId(7001L);
        material.setFormulaId(3001L);
        material.setMaterialId(4001L);
        material.setMaterialCode("MAT001");
        material.setMaterialNameCn("米色斑马帘面料");
        material.setUnitCode("米");
        material.setUsageMode("FORMULA");
        material.setUsageFormula("orderWidth * 12 - 2.0");
        material.setStatus("ENABLED");
        material.setDelFlag("0");
        return material;
    }

    private ProductFormulaOption option(String optionCode, String optionNameCn) {
        ProductFormulaOption option = new ProductFormulaOption();
        option.setFormulaId(3001L);
        option.setOptionCode(optionCode);
        option.setOptionNameCn(optionNameCn);
        option.setVisibilityMode("ALWAYS");
        option.setStatus("ENABLED");
        option.setDelFlag("0");
        return option;
    }

    private ProductFormulaOptionValue optionValue(String optionCode, String valueCode, String valueNameCn) {
        ProductFormulaOptionValue value = new ProductFormulaOptionValue();
        value.setFormulaId(3001L);
        value.setOptionCode(optionCode);
        value.setValueCode(valueCode);
        value.setValueNameCn(valueNameCn);
        value.setStatus("ENABLED");
        value.setDelFlag("0");
        return value;
    }

    private ProductFormulaRestriction restriction() {
        ProductFormulaRestriction restriction = new ProductFormulaRestriction();
        restriction.setFormulaId(3001L);
        restriction.setTargetOptionCode("FABRIC");
        restriction.setTargetValueCode("MAT001");
        restriction.setConditionType("WIDTH");
        restriction.setConditionOperator("GT");
        restriction.setConditionValueNumber(new BigDecimal("120"));
        restriction.setActionType("DISABLE");
        restriction.setStatus("ENABLED");
        restriction.setDelFlag("0");
        return restriction;
    }

    private ProductFormulaUsageRuleBo usageRuleBo(boolean defaultRule) {
        ProductFormulaUsageRuleBo bo = new ProductFormulaUsageRuleBo();
        bo.setMaterialCode("MAT001");
        bo.setRuleName(defaultRule ? "默认规则" : "米色面料规则");
        bo.setConditionType(defaultRule ? "DEFAULT" : "OPTION_VALUE");
        bo.setConditionOptionCode(defaultRule ? null : "FABRIC");
        bo.setConditionValueCode(defaultRule ? null : "MAT001");
        bo.setUsageMode("FORMULA");
        bo.setUsageFormula(defaultRule ? "1" : "orderWidth * 12 - 2.0");
        bo.setUsageFormulaText(defaultRule ? "1" : "订单宽 * 12 - 2.0");
        bo.setCalculationUnitCode("米");
        bo.setLossRate(new BigDecimal("5"));
        bo.setDefaultRuleFlag(defaultRule);
        bo.setStatus("ENABLED");
        return bo;
    }

    private ProductFormulaUsageRuleBo fixedUsageRuleBo() {
        ProductFormulaUsageRuleBo bo = new ProductFormulaUsageRuleBo();
        bo.setMaterialCode("MAT001");
        bo.setRuleName("固定用量");
        bo.setConditionType("DEFAULT");
        bo.setUsageMode("FIXED");
        bo.setFixedUsageQty(BigDecimal.ONE);
        bo.setCalculationUnitCode("米");
        bo.setDefaultRuleFlag(Boolean.TRUE);
        bo.setStatus("ENABLED");
        return bo;
    }

    private ProductFormulaUsageRule usageRule() {
        ProductFormulaUsageRule usageRule = new ProductFormulaUsageRule();
        usageRule.setFormulaId(3001L);
        usageRule.setMaterialCode("MAT001");
        usageRule.setRuleName("默认规则");
        usageRule.setConditionType("DEFAULT");
        usageRule.setUsageMode("FORMULA");
        usageRule.setUsageFormula("1");
        usageRule.setUsageFormulaText("1");
        usageRule.setDefaultRuleFlag(Boolean.TRUE);
        usageRule.setStatus("ENABLED");
        usageRule.setDelFlag("0");
        return usageRule;
    }

    private ProductFormula formula(String status) {
        ProductFormula formula = new ProductFormula();
        formula.setFormulaId(3001L);
        formula.setFormulaCode("FORMULA_25_ZEBRA");
        formula.setFormulaName("25英寸 9.5斑马帘标准配方");
        formula.setStatus(status);
        formula.setDelFlag("0");
        return formula;
    }

    private ProductMaterial material(Long id, String code) {
        ProductMaterial material = new ProductMaterial();
        material.setMaterialId(id);
        material.setMaterialCode(code);
        material.setMaterialNameCn("米色斑马帘面料");
        material.setSpecModelText("290cm 米色");
        material.setAttributeGroupId(100L);
        material.setAttributeGroupCode("FABRIC");
        material.setAttributeGroupNameCn("面料");
        material.setMaterialTypeId(200L);
        material.setMaterialTypeCode("FABRIC");
        material.setMaterialTypeNameCn("面料");
        material.setUnitCode("米");
        material.setStatus("ENABLED");
        material.setDelFlag("0");
        return material;
    }

    private ProductUnit unit() {
        ProductUnit unit = new ProductUnit();
        unit.setUnitId(5001L);
        unit.setUnitCode("米");
        unit.setUnitNameCn("米");
        unit.setStatus("ENABLED");
        unit.setDelFlag("0");
        return unit;
    }
}
