package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.entity.ProductFormulaVariable;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;

import java.util.List;

record ProductFormulaSetupContext(
    List<ProductFormulaMaterial> materials,
    List<ProductFormulaOption> options,
    List<ProductFormulaOptionValue> values,
    List<ProductFormulaOptionMaterial> optionMaterials,
    List<ProductFormulaRestriction> restrictions,
    List<ProductFormulaUsageRule> usageRules,
    List<ProductFormulaVariable> variables,
    List<ProductFormulaVariableRule> variableRules
) {
}
