package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;

public interface ProductPriceRuntimeContext {
    Long tenantId();

    Long saleProductId();

    Long formulaVersionId();

    String currencyCode();

    ProductPriceSetupVo setup();

    ProductPriceQuoteVo quote(ProductPriceQuoteBo request);
}
