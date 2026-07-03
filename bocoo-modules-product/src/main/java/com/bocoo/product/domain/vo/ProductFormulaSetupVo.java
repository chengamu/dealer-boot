package com.bocoo.product.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class ProductFormulaSetupVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private ProductFormulaVo formula;
    private List<ProductFormulaMaterialVo> materials = new ArrayList<>();
    private List<ProductFormulaOptionVo> options = new ArrayList<>();
    private List<ProductFormulaOptionValueVo> optionValues = new ArrayList<>();
    private List<ProductFormulaOptionMaterialVo> optionMaterials = new ArrayList<>();
    private List<ProductFormulaRestrictionVo> restrictions = new ArrayList<>();
    private List<ProductFormulaUsageRuleVo> usageRules = new ArrayList<>();
    private List<ProductFormulaVariableVo> variables = new ArrayList<>();
    private List<ProductFormulaVariableRuleVo> variableRules = new ArrayList<>();
}
