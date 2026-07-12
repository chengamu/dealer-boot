package com.bocoo.product.service.impl;

import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.service.ProductPriceRuntimeContext;

import java.util.List;

record ProductPriceRuntimeContextImpl(
    ProductSaleProduct saleProduct,
    ProductPriceSetting setting,
    ProductFormulaVersion version,
    ProductFormulaSetupVo formulaSetup,
    List<ProductPriceMaterial> materials,
    List<ProductPriceMaterialRule> rules,
    ProductPriceSetupVo setup,
    ProductPricingEngine engine
) implements ProductPriceRuntimeContext {
    @Override public Long tenantId() { return saleProduct.getTenantId(); }
    @Override public Long saleProductId() { return saleProduct.getSaleProductId(); }
    @Override public Long formulaVersionId() { return version.getVersionId(); }
    @Override public String currencyCode() { return setting.getCurrencyCode(); }
    @Override public ProductPriceQuoteVo quote(ProductPriceQuoteBo request) {
        return engine.quote(saleProduct, setting, version, formulaSetup, materials, rules, request);
    }
}
