package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductFormulaVariableBo;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.entity.ProductFormulaVariable;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;
import com.bocoo.product.domain.vo.ProductFormulaVariableRuleVo;
import com.bocoo.product.domain.vo.ProductFormulaVariableVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaUsageRuleMapper;
import com.bocoo.product.mapper.ProductFormulaVariableMapper;
import com.bocoo.product.mapper.ProductFormulaVariableRuleMapper;
import com.bocoo.product.service.impl.ProductFormulaVariableServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ProductFormulaVariableServiceImplTest {

    private ProductFormulaVariableServiceImpl service;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        service = new ProductFormulaVariableServiceImpl(
            mock(ProductFormulaMapper.class),
            mock(ProductFormulaVariableMapper.class),
            mock(ProductFormulaVariableRuleMapper.class),
            mock(ProductFormulaUsageRuleMapper.class)
        );
    }

    @Test
    void variableCodeMustBeUniqueInSameFormula() {
        assertThatThrownBy(() -> service.normalizeVariables(3001L, List.of(
            variableBo("ALU_DEDUCT", "铝材扣减", 10),
            variableBo("ALU_DEDUCT", "面料扣减", 20)
        ))).isInstanceOf(ServiceException.class);

        assertThat(service.normalizeVariables(3001L, List.of(
            variableBo("ALU_DEDUCT", "铝材扣减", 10),
            variableBo("FABRIC_DEDUCT", "铝材扣减", 20)
        ))).hasSize(2);
    }

    @Test
    void variableRequiresDefaultRuleAndRejectsMissingReferences() {
        List<ProductFormulaVariable> variables = List.of(variable("ALU_DEDUCT", "铝材扣减", 10));

        assertThat(service.validationMessageKey(variables, List.of(), List.of()))
            .isEqualTo("product.formula.variableDefaultRuleRequired");

        assertThat(service.validationMessageKey(variables, List.of(formulaRule("ALU_DEDUCT", "var_UNKNOWN + 1", true)), List.of()))
            .isEqualTo("product.formula.variableReferenceInvalid");
    }

    @Test
    void variableCycleIsRejected() {
        List<ProductFormulaVariable> variables = List.of(
            variable("A", "变量A", 10),
            variable("B", "变量B", 20)
        );
        List<ProductFormulaVariableRule> rules = List.of(
            formulaRule("A", "var_B + 1", true),
            formulaRule("B", "var_A + 1", true)
        );

        assertThat(service.validationMessageKey(variables, rules, List.of()))
            .isEqualTo("product.formula.variableCycleInvalid");
    }

    @Test
    void usageFormulaCanReferenceExistingVariable() {
        ProductFormulaUsageRule usageRule = new ProductFormulaUsageRule();
        usageRule.setUsageFormula("orderWidthCm - var_ALU_DEDUCT");

        assertThat(service.validationMessageKey(
            List.of(variable("ALU_DEDUCT", "铝材扣减", 10)),
            List.of(fixedRule("ALU_DEDUCT", 8.1, true)),
            List.of(usageRule)
        )).isNull();

        assertThat(service.validationMessageKey(List.of(), List.of(), List.of(usageRule)))
            .isEqualTo("product.formula.variableReferenceInvalid");
    }

    @Test
    void evaluatesFixedFormulaAndRoundingVariables() {
        ProductFormulaVariableVo deduct = variableVo("ALU_DEDUCT", "铝材扣减", 10);
        ProductFormulaVariableVo bead = variableVo("BEAD_COUNT", "走珠数量", 20);
        ProductFormulaVariableRuleVo deductRule = fixedRuleVo("ALU_DEDUCT", 8.1, true);
        ProductFormulaVariableRuleVo beadRule = formulaRuleVo("BEAD_COUNT", "ceil(orderWidthCm / 10.5)", true);

        Map<String, Object> result = service.evaluateVariables(
            List.of(deduct, bead),
            List.of(deductRule, beadRule),
            Map.of("orderWidthCm", 25D)
        );

        assertThat(result.get("var_ALU_DEDUCT")).isEqualTo(8.1D);
        assertThat(result.get("var_BEAD_COUNT")).isEqualTo(3D);
    }

    private ProductFormulaVariableBo variableBo(String code, String name, int sortOrder) {
        ProductFormulaVariableBo bo = new ProductFormulaVariableBo();
        bo.setVariableCode(code);
        bo.setVariableName(name);
        bo.setSortOrder(sortOrder);
        return bo;
    }

    private ProductFormulaVariable variable(String code, String name, int sortOrder) {
        ProductFormulaVariable row = new ProductFormulaVariable();
        row.setVariableCode(code);
        row.setVariableName(name);
        row.setSortOrder(sortOrder);
        return row;
    }

    private ProductFormulaVariableVo variableVo(String code, String name, int sortOrder) {
        ProductFormulaVariableVo row = new ProductFormulaVariableVo();
        row.setVariableCode(code);
        row.setVariableName(name);
        row.setSortOrder(sortOrder);
        return row;
    }

    private ProductFormulaVariableRule fixedRule(String code, double value, boolean defaultRule) {
        ProductFormulaVariableRule row = new ProductFormulaVariableRule();
        row.setVariableCode(code);
        row.setValueType("FIXED");
        row.setFixedValue(BigDecimal.valueOf(value));
        row.setDefaultRuleFlag(defaultRule);
        return row;
    }

    private ProductFormulaVariableRule formulaRule(String code, String expression, boolean defaultRule) {
        ProductFormulaVariableRule row = fixedRule(code, 0D, defaultRule);
        row.setValueType("FORMULA");
        row.setFormulaExpression(expression);
        return row;
    }

    private ProductFormulaVariableRuleVo fixedRuleVo(String code, double value, boolean defaultRule) {
        ProductFormulaVariableRuleVo row = new ProductFormulaVariableRuleVo();
        row.setVariableCode(code);
        row.setValueType("FIXED");
        row.setFixedValue(BigDecimal.valueOf(value));
        row.setDefaultRuleFlag(defaultRule);
        return row;
    }

    private ProductFormulaVariableRuleVo formulaRuleVo(String code, String expression, boolean defaultRule) {
        ProductFormulaVariableRuleVo row = fixedRuleVo(code, 0D, defaultRule);
        row.setValueType("FORMULA");
        row.setFormulaExpression(expression);
        return row;
    }
}
