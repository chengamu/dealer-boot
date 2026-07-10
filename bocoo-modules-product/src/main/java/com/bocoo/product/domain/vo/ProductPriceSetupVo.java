package com.bocoo.product.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class ProductPriceSetupVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private ProductSaleProductVo saleProduct;
    private ProductPriceSettingVo setting;
    private List<ProductMaterialVo> fabricCandidates = new ArrayList<>();
    private List<ProductPriceOptionCombinationVo> fabricPriceColumns = new ArrayList<>();
    private List<ProductPriceFabricVo> priceFabrics = new ArrayList<>();
    private List<ProductPriceFabricRuleVo> fabricRules = new ArrayList<>();
    private List<ProductPriceValidationIssueVo> issues = new ArrayList<>();
    private List<ProductFormulaMaterialVo> formulaMaterials = new ArrayList<>();
    private List<ProductFormulaOptionVo> formulaOptions = new ArrayList<>();
    private List<ProductFormulaOptionValueVo> formulaOptionValues = new ArrayList<>();
    private List<ProductFormulaOptionMaterialVo> formulaOptionMaterials = new ArrayList<>();
    private Map<String, Integer> materialGroupCounts = new LinkedHashMap<>();
}
