package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;

import java.util.List;

record ProductFormulaSetupRows(
    List<ProductFormulaMaterial> materials,
    List<ProductFormulaOption> options,
    List<ProductFormulaOptionValue> values,
    List<ProductFormulaOptionMaterial> optionMaterials,
    List<ProductFormulaRestriction> restrictions,
    List<ProductFormulaUsageRule> usageRules
) {}
