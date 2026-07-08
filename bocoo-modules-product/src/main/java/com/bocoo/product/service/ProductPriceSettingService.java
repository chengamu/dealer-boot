package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductPriceFabricRuleBo;
import com.bocoo.product.domain.bo.ProductPriceFeeRuleBo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;

import java.util.List;

public interface ProductPriceSettingService {
    ProductPriceSetupVo querySetup(Long saleProductId);

    Boolean generateFabricPrices(Long saleProductId, Boolean overwrite);

    Boolean saveFabricRules(Long saleProductId, List<ProductPriceFabricRuleBo> rules);

    Boolean saveFeeRules(Long saleProductId, List<ProductPriceFeeRuleBo> rules);

    List<ProductPriceValidationIssueVo> validatePrice(Long saleProductId);
}
