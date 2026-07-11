package com.bocoo.product.domain.bo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductPriceMaterialBatchRuleBo {
    private List<Long> priceMaterialIds = new ArrayList<>();
    private List<ProductPriceMaterialRuleBo> rules = new ArrayList<>();
}
