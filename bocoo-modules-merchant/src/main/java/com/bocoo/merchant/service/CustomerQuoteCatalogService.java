package com.bocoo.merchant.service;

import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;

import java.util.List;
import com.bocoo.merchant.domain.vo.CustomerQuoteCatalogSetupVo;

public interface CustomerQuoteCatalogService {

    List<ProductSaleProductVo> queryProducts(ProductSaleProductBo bo);

    ProductPriceSetupVo querySetup(Long saleProductId);

    CustomerQuoteCatalogSetupVo queryCatalogSetup(Long saleProductId);

    ProductPriceQuoteVo quote(Long saleProductId, ProductPriceQuoteBo bo);

    List<ProductShippingTemplateVo> queryShippingTemplates(ProductShippingTemplateBo bo);

    ProductShippingTemplateVo queryShippingTemplate(Long id);
}
