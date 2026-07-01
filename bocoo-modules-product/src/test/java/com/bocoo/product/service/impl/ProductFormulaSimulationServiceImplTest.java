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
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.service.ProductFormulaSetupService;
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
    private ProductFormulaSimulationServiceImpl service;

    @BeforeEach
    void setUp() {
        formulaMapper = mock(ProductFormulaMapper.class);
        productMaterialMapper = mock(ProductMaterialMapper.class);
        setupService = mock(ProductFormulaSetupService.class);
        service = new ProductFormulaSimulationServiceImpl(
            formulaMapper,
            setupService,
            new ProductFormulaSimulationEngine(productMaterialMapper, new ProductFormulaSimulationUsageCalculator())
        );
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

        ProductFormulaSimulationVo result = service.run(3001L, simulationBo(Map.of("FABRIC", "MAT001")));

        assertThat(result.getStatus()).isEqualTo("PASS");
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getMaterialCode()).isEqualTo("MAT001");
        assertThat(result.getItems().get(0).getUsageQty()).isEqualByComparingTo("20.00");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("60.00");
    }

    @Test
    void runFailsWhenDisableRestrictionIsHit() {
        ProductFormulaSimulationBo bo = simulationBo(Map.of("FABRIC", "MAT001"));
        bo.setOrderWidth(new BigDecimal("130"));

        ProductFormulaSimulationVo result = service.run(3001L, bo);

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

    private ProductFormulaMaterialVo material() {
        ProductFormulaMaterialVo material = new ProductFormulaMaterialVo();
        material.setFormulaMaterialId(7001L);
        material.setMaterialId(4001L);
        material.setMaterialCode("MAT001");
        material.setMaterialNameCn("米色斑马帘面料");
        material.setUnitCode("米");
        material.setRequiredFlag(Boolean.FALSE);
        material.setFixedUsageQty(BigDecimal.ONE);
        material.setStatus("ENABLED");
        material.setSortOrder(10);
        return material;
    }

    private ProductFormulaOptionVo option() {
        ProductFormulaOptionVo option = new ProductFormulaOptionVo();
        option.setOptionCode("FABRIC");
        option.setOptionNameCn("面料");
        option.setVisibilityMode("ALWAYS");
        option.setRequiredFlag(Boolean.TRUE);
        option.setStatus("ENABLED");
        return option;
    }

    private ProductFormulaOptionValueVo optionValue() {
        ProductFormulaOptionValueVo value = new ProductFormulaOptionValueVo();
        value.setOptionCode("FABRIC");
        value.setValueCode("MAT001");
        value.setValueNameCn("米色斑马帘面料");
        value.setStatus("ENABLED");
        return value;
    }

    private ProductFormulaOptionMaterialVo optionMaterial() {
        ProductFormulaOptionMaterialVo optionMaterial = new ProductFormulaOptionMaterialVo();
        optionMaterial.setOptionCode("FABRIC");
        optionMaterial.setValueCode("MAT001");
        optionMaterial.setFormulaMaterialId(7001L);
        optionMaterial.setMaterialId(4001L);
        optionMaterial.setMaterialCode("MAT001");
        optionMaterial.setStatus("ENABLED");
        return optionMaterial;
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
        rule.setUsageFormula(defaultRule ? "1" : "orderWidth * 2");
        rule.setDefaultRuleFlag(defaultRule);
        rule.setStatus("ENABLED");
        rule.setSortOrder(defaultRule ? 20 : 10);
        return rule;
    }
}
