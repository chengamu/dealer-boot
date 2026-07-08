package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.mapper.ProductFormulaUsageRuleMapper;
import com.bocoo.product.mapper.ProductBaseAttributeMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ProductFormulaUsageRuleServiceImplTest {

    private final ProductFormulaUsageRuleServiceImpl service =
        new ProductFormulaUsageRuleServiceImpl(mock(ProductFormulaUsageRuleMapper.class), new ProductFormulaUsageRuleValidator(
            new ProductFormulaExpressionReferenceValidator(mock(ProductBaseAttributeMapper.class))));

    @Test
    void fixedUsageModeAllowsOnlyOneEnabledRulePerMaterial() {
        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(
            fixedRule(),
            fixedRule()
        ))).isEqualTo("product.formula.usageRuleDuplicate");
    }

    @Test
    void formulaUsageModeRequiresOneDefaultRule() {
        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(
            formulaRule(false)
        ))).isEqualTo("product.formula.usageDefaultRuleRequired");

        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(
            formulaRule(true),
            formulaRule(true)
        ))).isEqualTo("product.formula.usageRuleDuplicate");
    }

    @Test
    void usageModesCannotBeMixedForSameMaterial() {
        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(
            fixedRule(),
            formulaRule(true)
        ))).isEqualTo("product.formula.usageModeMixedInvalid");
    }

    @Test
    void formulaRuleRejectsInvalidFormulaAndNonBooleanExpressionCondition() {
        ProductFormulaUsageRule invalidFormula = formulaRule(true);
        invalidFormula.setUsageFormula("if(orderWidthIn > 12, 2, 1)");
        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(invalidFormula)))
            .isEqualTo("product.formula.usageFormulaInvalid");

        ProductFormulaUsageRule invalidCondition = formulaRule(false);
        invalidCondition.setConditionType("EXPRESSION");
        invalidCondition.setConditionExpression("orderWidthIn");
        invalidCondition.setConditionKey("EXPR:orderWidthIn");
        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(invalidCondition)))
            .isEqualTo("product.formula.usageConditionInvalid");
    }

    @Test
    void formulaRuleCanUseDimensionFormulasWithoutQuantityFormula() {
        ProductFormulaUsageRule rule = formulaRule(true);
        rule.setUsageFormula(null);
        rule.setWidthFormula("orderWidthCm + 3");
        rule.setHeightFormula("orderHeightCm + 3");

        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(rule))).isNull();
    }

    @Test
    void formulaRuleRequiresAtLeastOneFormulaField() {
        ProductFormulaUsageRule rule = formulaRule(true);
        rule.setUsageFormula(null);
        rule.setLengthFormula(null);
        rule.setWidthFormula(null);
        rule.setHeightFormula(null);
        rule.setWeightFormula(null);

        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(rule)))
            .isEqualTo("product.formula.materialUsageRuleRequired");
    }

    @Test
    void optionValueConditionMustReferenceExistingOptionValue() {
        ProductFormulaUsageRule rule = formulaRule(false);
        rule.setConditionValueCode("UNKNOWN");

        assertThat(service.validationMessageKey(materials(), options(), values(), optionMaterials(), List.of(rule)))
            .isEqualTo("product.formula.usageConditionInvalid");
    }

    private List<ProductFormulaMaterial> materials() {
        ProductFormulaMaterial material = new ProductFormulaMaterial();
        material.setFormulaMaterialId(7001L);
        material.setMaterialId(4001L);
        material.setMaterialCode("MAT001");
        material.setMaterialNameCn("遮光面料");
        material.setStatus("ENABLED");
        material.setDelFlag("0");
        return List.of(material);
    }

    private List<ProductFormulaOption> options() {
        ProductFormulaOption option = new ProductFormulaOption();
        option.setOptionCode("FABRIC");
        option.setOptionNameCn("面料");
        option.setStatus("ENABLED");
        option.setDelFlag("0");
        return List.of(option);
    }

    private List<ProductFormulaOptionValue> values() {
        ProductFormulaOptionValue value = new ProductFormulaOptionValue();
        value.setOptionCode("FABRIC");
        value.setValueCode("MAT001");
        value.setValueNameCn("遮光面料");
        value.setStatus("ENABLED");
        value.setDelFlag("0");
        return List.of(value);
    }

    private List<ProductFormulaOptionMaterial> optionMaterials() {
        ProductFormulaOptionMaterial row = new ProductFormulaOptionMaterial();
        row.setOptionCode("FABRIC");
        row.setValueCode("MAT001");
        row.setMaterialCode("MAT001");
        row.setStatus("ENABLED");
        row.setDelFlag("0");
        return List.of(row);
    }

    private ProductFormulaUsageRule fixedRule() {
        ProductFormulaUsageRule rule = baseRule();
        rule.setConditionType("DEFAULT");
        rule.setConditionExpression("DEFAULT");
        rule.setConditionKey("DEFAULT");
        rule.setUsageMode("FIXED");
        rule.setFixedUsageQty(BigDecimal.ONE);
        rule.setDefaultRuleFlag(Boolean.TRUE);
        return rule;
    }

    private ProductFormulaUsageRule formulaRule(boolean defaultRule) {
        ProductFormulaUsageRule rule = baseRule();
        rule.setConditionType(defaultRule ? "DEFAULT" : "OPTION_VALUE");
        rule.setConditionOptionCode(defaultRule ? null : "FABRIC");
        rule.setConditionValueCode(defaultRule ? null : "MAT001");
        rule.setConditionExpression(defaultRule ? "DEFAULT" : "fabric == \"MAT001\"");
        rule.setConditionKey(defaultRule ? "DEFAULT" : "OPTION:FABRIC:MAT001");
        rule.setUsageMode("FORMULA");
        rule.setUsageFormula("orderWidthCm * 12 - 2.0");
        rule.setDefaultRuleFlag(defaultRule);
        return rule;
    }

    private ProductFormulaUsageRule baseRule() {
        ProductFormulaUsageRule rule = new ProductFormulaUsageRule();
        rule.setFormulaId(3001L);
        rule.setMaterialCode("MAT001");
        rule.setStatus("ENABLED");
        rule.setDelFlag("0");
        return rule;
    }
}
