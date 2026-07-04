package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductFormulaUsageRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.vo.ProductFormulaUsageRuleVo;

import java.util.List;

public interface ProductFormulaUsageRuleService {

    List<ProductFormulaUsageRuleVo> queryByFormula(Long formulaId);

    List<ProductFormulaUsageRule> activeRules(Long formulaId);

    List<ProductFormulaUsageRule> normalize(Long formulaId, List<ProductFormulaMaterial> materials,
                                            List<ProductFormulaOption> options, List<ProductFormulaOptionValue> values,
                                            List<ProductFormulaOptionMaterial> optionMaterials, List<ProductFormulaUsageRuleBo> rows);

    void insertAll(List<ProductFormulaUsageRule> rules, List<ProductFormulaMaterial> materials);

    void deleteByFormula(Long formulaId);

    String validationMessageKey(List<ProductFormulaMaterial> materials, List<ProductFormulaOption> options,
                                List<ProductFormulaOptionValue> values, List<ProductFormulaOptionMaterial> optionMaterials,
                                List<ProductFormulaUsageRule> usageRules);
}
