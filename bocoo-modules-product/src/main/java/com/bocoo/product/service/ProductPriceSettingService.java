package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductPriceMaterialBatchRuleBo;
import com.bocoo.product.domain.bo.ProductPriceMaterialRuleBo;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;

import java.util.List;

public interface ProductPriceSettingService {
    ProductPriceSetupVo querySetup(Long saleProductId);

    Boolean generateMaterialPrices(Long saleProductId, Boolean overwrite);

    Boolean saveMaterialRules(Long saleProductId, Long priceMaterialId, List<ProductPriceMaterialRuleBo> rules);

    Boolean saveMaterialRulesBatch(Long saleProductId, ProductPriceMaterialBatchRuleBo batch);

    List<ProductPriceValidationIssueVo> validatePrice(Long saleProductId);

    ProductPriceQuoteVo quote(Long saleProductId, ProductPriceQuoteBo quote);
}
