package com.bocoo.product.domain.bo;

import jakarta.validation.Valid;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductFormulaSetupBo {
    @Valid
    private List<ProductFormulaMaterialBo> materials = new ArrayList<>();
    @Valid
    private List<ProductFormulaOptionBo> options = new ArrayList<>();
    @Valid
    private List<ProductFormulaOptionValueBo> optionValues = new ArrayList<>();
    @Valid
    private List<ProductFormulaOptionMaterialBo> optionMaterials = new ArrayList<>();
    @Valid
    private List<ProductFormulaRestrictionBo> restrictions = new ArrayList<>();
    @Valid
    private List<ProductFormulaUsageRuleBo> usageRules = new ArrayList<>();
    @Valid
    private List<ProductFormulaVariableBo> variables = new ArrayList<>();
    @Valid
    private List<ProductFormulaVariableRuleBo> variableRules = new ArrayList<>();
}
