package com.bocoo.product.service.impl;

import com.bocoo.product.domain.bo.ProductFormulaSimulationBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.vo.ProductFormulaMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import com.bocoo.product.domain.vo.ProductFormulaRestrictionVo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationVo;
import com.bocoo.product.domain.vo.ProductFormulaUsageRuleVo;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.service.ProductFormulaSetupService;
import com.bocoo.product.service.ProductFormulaVariableService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductFormulaSimulationServiceImplTest {

    private ProductFormulaMapper formulaMapper;
    private ProductMaterialMapper productMaterialMapper;
    private ProductFormulaSetupService setupService;
    private ProductFormulaVariableService variableService;
    private ProductFormulaSimulationServiceImpl service;

    @BeforeEach
    void setUp() {
        formulaMapper = mock(ProductFormulaMapper.class);
        productMaterialMapper = mock(ProductMaterialMapper.class);
        setupService = mock(ProductFormulaSetupService.class);
        variableService = mock(ProductFormulaVariableService.class);
        service = new ProductFormulaSimulationServiceImpl(
            formulaMapper,
            setupService,
            new ProductFormulaSimulationEngine(productMaterialMapper, new ProductFormulaSimulationUsageCalculator(), variableService)
        );
        when(variableService.evaluateVariables(any(), any(), any())).thenAnswer(invocation -> invocation.getArgument(2));
        when(formulaMapper.selectById(3001L)).thenReturn(formula());
        when(setupService.validationMessageKey(3001L)).thenReturn(null);
        when(setupService.querySetup(3001L)).thenReturn(setup());
    }

    @Test
    void runFailsWhenVisibleRequiredOptionIsMissing() {
        ProductFormulaSimulationVo result = service.run(3001L, simulationBo(Map.of()));

        assertThat(result.getStatus()).isEqualTo("FAIL");
        assertThat(result.getMessage()).isEqualTo("product.formula.simulationOptionRequired");
    }

    @Test
    void runBuildsBomFromRequiredAndSelectedOptionMaterialsWithConditionalUsageRule() {
        ProductMaterial price = new ProductMaterial();
        price.setMaterialId(4001L);
        price.setSalesPrice(new BigDecimal("3.00"));
        when(productMaterialMapper.selectList(any())).thenReturn(List.of(price));

        ProductFormulaSimulationBo bo = simulationBo(Map.of("FABRIC", "MAT001"));
        bo.setOrderQuantity(2);

        ProductFormulaSimulationVo result = service.run(3001L, bo);

        assertThat(result.getStatus()).isEqualTo("PASS");
        assertThat(result.getOrderQuantity()).isEqualTo(2);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getMaterialCode()).isEqualTo("MAT001");
        assertThat(result.getItems().get(0).getUsageQty()).isEqualByComparingTo("50.80");
        assertThat(result.getSingleAmount()).isEqualByComparingTo("152.40");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("304.80");
    }

    @Test
    void runFailsWhenDisableRestrictionIsHit() {
        ProductFormulaSimulationBo bo = simulationBo(Map.of("FABRIC", "MAT001"));
        bo.setOrderWidth(new BigDecimal("130"));

        ProductFormulaSimulationVo result = service.run(3001L, bo);

        assertThat(result.getStatus()).isEqualTo("FAIL");
        assertThat(result.getMessage()).isEqualTo("product.formula.simulationRestrictionHit");
    }

    @Test
    void runDoesNotBringDisabledOptionMaterials() {
        ProductFormulaSetupVo setup = setup();
        setup.getOptionMaterials().get(0).setStatus("DISABLED");
        when(setupService.querySetup(3001L)).thenReturn(setup);

        ProductFormulaSimulationVo result = service.run(3001L, simulationBo(Map.of("FABRIC", "MAT001")));

        assertThat(result.getStatus()).isEqualTo("FAIL");
        assertThat(result.getMessage()).isEqualTo("product.formula.notConfigured");
    }

    @Test
    void runIgnoresDisabledRestrictions() {
        ProductFormulaSetupVo setup = setup();
        setup.getRestrictions().get(0).setStatus("DISABLED");
        when(setupService.querySetup(3001L)).thenReturn(setup);
        ProductMaterial price = new ProductMaterial();
        price.setMaterialId(4001L);
        price.setSalesPrice(new BigDecimal("3.00"));
        when(productMaterialMapper.selectList(any())).thenReturn(List.of(price));
        ProductFormulaSimulationBo bo = simulationBo(Map.of("FABRIC", "MAT001"));
        bo.setOrderWidth(new BigDecimal("130"));

        ProductFormulaSimulationVo result = service.run(3001L, bo);

        assertThat(result.getStatus()).isEqualTo("PASS");
    }

    @Test
    void runEvaluatesSelectedOptionMaterialAttributesInRestrictionExpression() {
        when(setupService.querySetup(3001L)).thenReturn(setupWithThicknessRestriction());

        ProductFormulaSimulationVo result = service.run(3001L, simulationBo(Map.of(
            "FABRIC", "MAT001",
            "SECONDARY_FABRIC", "MAT002"
        )));

        assertThat(result.getStatus()).isEqualTo("FAIL");
        assertThat(result.getMessage()).isEqualTo("product.formula.simulationRestrictionHit");
    }

    private ProductFormula formula() {
        ProductFormula formula = new ProductFormula();
        formula.setFormulaId(3001L);
        formula.setProductTypeCode("CUSTOM_CURTAIN");
        formula.setMinWidthInch(BigDecimal.ZERO);
        formula.setMinHeightInch(BigDecimal.ZERO);
        formula.setMaxWidthInch(new BigDecimal("150"));
        formula.setMaxHeightInch(new BigDecimal("150"));
        formula.setStatus("DRAFT");
        return formula;
    }

    private ProductFormulaSimulationBo simulationBo(Map<String, String> selectedValues) {
        ProductFormulaSimulationBo bo = new ProductFormulaSimulationBo();
        bo.setOrderWidth(new BigDecimal("10"));
        bo.setOrderHeight(new BigDecimal("20"));
        bo.setSelectedOptionValues(new LinkedHashMap<>(selectedValues));
        return bo;
    }

    private ProductFormulaSetupVo setup() {
        ProductFormulaSetupVo setup = new ProductFormulaSetupVo();
        setup.setMaterials(List.of(material()));
        setup.setOptions(List.of(option()));
        setup.setOptionValues(List.of(optionValue()));
        setup.setOptionMaterials(List.of(optionMaterial()));
        setup.setRestrictions(List.of(restriction()));
        setup.setUsageRules(List.of(usageRule(false), usageRule(true)));
        return setup;
    }

    private ProductFormulaSetupVo setupWithThicknessRestriction() {
        ProductFormulaSetupVo setup = new ProductFormulaSetupVo();
        ProductFormulaMaterialVo main = material();
        main.setAttributeList(List.of(attribute("THICKNESS", "厚度", "8")));
        ProductFormulaMaterialVo secondary = material("MAT002", 4002L, 7002L);
        secondary.setAttributeList(List.of(attribute("THICKNESS", "厚度", "6")));
        setup.setMaterials(List.of(main, secondary));
        setup.setOptions(List.of(option(), option("SECONDARY_FABRIC", "副面料")));
        setup.setOptionValues(List.of(optionValue(), optionValue("SECONDARY_FABRIC", "MAT002", "副面料")));
        setup.setOptionMaterials(List.of(optionMaterial(), optionMaterial("SECONDARY_FABRIC", "MAT002", 7002L, 4002L)));
        ProductFormulaRestrictionVo restriction = new ProductFormulaRestrictionVo();
        restriction.setConditionType("EXPRESSION");
        restriction.setConditionExpression("material_FABRIC_THICKNESS + material_SECONDARY_FABRIC_THICKNESS > 12");
        restriction.setActionType("DISABLE");
        restriction.setStatus("ENABLED");
        setup.setRestrictions(List.of(restriction));
        return setup;
    }

    private ProductFormulaMaterialVo material() {
        return material("MAT001", 4001L, 7001L);
    }

    private ProductFormulaMaterialVo material(String materialCode, Long materialId, Long formulaMaterialId) {
        ProductFormulaMaterialVo material = new ProductFormulaMaterialVo();
        material.setFormulaMaterialId(formulaMaterialId);
        material.setMaterialId(materialId);
        material.setMaterialCode(materialCode);
        material.setMaterialNameCn(materialCode);
        material.setUnitCode("米");
        material.setRequiredFlag(Boolean.FALSE);
        material.setFixedUsageQty(BigDecimal.ONE);
        material.setStatus("ENABLED");
        material.setSortOrder(10);
        return material;
    }

    private ProductFormulaOptionVo option() {
        return option("FABRIC", "面料");
    }

    private ProductFormulaOptionVo option(String optionCode, String optionNameCn) {
        ProductFormulaOptionVo option = new ProductFormulaOptionVo();
        option.setOptionCode(optionCode);
        option.setOptionNameCn(optionNameCn);
        option.setVisibilityMode("ALWAYS");
        option.setRequiredFlag(Boolean.TRUE);
        option.setStatus("ENABLED");
        return option;
    }

    private ProductFormulaOptionValueVo optionValue() {
        return optionValue("FABRIC", "MAT001", "米色斑马帘面料");
    }

    private ProductFormulaOptionValueVo optionValue(String optionCode, String valueCode, String valueNameCn) {
        ProductFormulaOptionValueVo value = new ProductFormulaOptionValueVo();
        value.setOptionCode(optionCode);
        value.setValueCode(valueCode);
        value.setValueNameCn(valueNameCn);
        value.setStatus("ENABLED");
        return value;
    }

    private ProductFormulaOptionMaterialVo optionMaterial() {
        return optionMaterial("FABRIC", "MAT001", 7001L, 4001L);
    }

    private ProductFormulaOptionMaterialVo optionMaterial(String optionCode, String valueCode, Long formulaMaterialId, Long materialId) {
        ProductFormulaOptionMaterialVo optionMaterial = new ProductFormulaOptionMaterialVo();
        optionMaterial.setOptionCode(optionCode);
        optionMaterial.setValueCode(valueCode);
        optionMaterial.setFormulaMaterialId(formulaMaterialId);
        optionMaterial.setMaterialId(materialId);
        optionMaterial.setMaterialCode(valueCode);
        optionMaterial.setStatus("ENABLED");
        return optionMaterial;
    }

    private ProductMaterialAttributeVo attribute(String code, String nameCn, String value) {
        ProductMaterialAttributeVo attribute = new ProductMaterialAttributeVo();
        attribute.setAttributeCode(code);
        attribute.setAttributeNameCn(nameCn);
        attribute.setValueNumber(new BigDecimal(value));
        return attribute;
    }

    private ProductFormulaRestrictionVo restriction() {
        ProductFormulaRestrictionVo restriction = new ProductFormulaRestrictionVo();
        restriction.setTargetOptionCode("FABRIC");
        restriction.setTargetValueCode("MAT001");
        restriction.setConditionType("WIDTH");
        restriction.setConditionOperator("GT");
        restriction.setConditionValueNumber(new BigDecimal("120"));
        restriction.setActionType("DISABLE");
        restriction.setStatus("ENABLED");
        return restriction;
    }

    private ProductFormulaUsageRuleVo usageRule(boolean defaultRule) {
        ProductFormulaUsageRuleVo rule = new ProductFormulaUsageRuleVo();
        rule.setFormulaMaterialId(7001L);
        rule.setMaterialCode("MAT001");
        rule.setRuleName(defaultRule ? "默认规则" : "面料规则");
        rule.setConditionType(defaultRule ? "DEFAULT" : "OPTION_VALUE");
        rule.setConditionOptionCode(defaultRule ? null : "FABRIC");
        rule.setConditionValueCode(defaultRule ? null : "MAT001");
        rule.setUsageMode("FORMULA");
        rule.setUsageFormula(defaultRule ? "1" : "orderWidthCm * 2");
        rule.setDefaultRuleFlag(defaultRule);
        rule.setStatus("ENABLED");
        rule.setSortOrder(defaultRule ? 20 : 10);
        return rule;
    }
}
