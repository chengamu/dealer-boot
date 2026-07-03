package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.bo.ProductFormulaVariableBo;
import com.bocoo.product.domain.bo.ProductFormulaVariableRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.entity.ProductFormulaVariable;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductFormulaVariableRuleVo;
import com.bocoo.product.domain.vo.ProductFormulaVariableVo;

import java.util.List;
import java.util.Map;

public interface ProductFormulaVariableService {

    ProductFormulaSetupVo queryVariables(Long formulaId);

    Boolean saveVariables(Long formulaId, ProductFormulaSetupBo bo);

    Boolean copyFrom(Long formulaId, String sourceFormulaCode);

    Boolean validateVariables(Long formulaId);

    List<ProductFormulaVariableVo> queryVariableVos(Long formulaId);

    List<ProductFormulaVariableRuleVo> queryRuleVos(Long formulaId);

    List<ProductFormulaVariable> activeVariables(Long formulaId);

    List<ProductFormulaVariableRule> activeRules(Long formulaId);

    List<ProductFormulaVariable> normalizeVariables(Long formulaId, List<ProductFormulaVariableBo> rows);

    List<ProductFormulaVariableRule> normalizeRules(Long formulaId, List<ProductFormulaVariable> variables,
                                                    List<ProductFormulaVariableRuleBo> rows);

    void replace(Long formulaId, List<ProductFormulaVariable> variables, List<ProductFormulaVariableRule> rules);

    void insertAll(List<ProductFormulaVariable> variables, List<ProductFormulaVariableRule> rules);

    String validationMessageKey(List<ProductFormulaVariable> variables, List<ProductFormulaVariableRule> rules,
                                List<ProductFormulaUsageRule> usageRules);

    Map<String, Object> evaluateVariables(List<ProductFormulaVariableVo> variables, List<ProductFormulaVariableRuleVo> rules,
                                          Map<String, Object> context);
}
