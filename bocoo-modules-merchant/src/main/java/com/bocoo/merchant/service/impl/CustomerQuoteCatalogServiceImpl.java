package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.merchant.domain.vo.CustomerQuoteCatalogSetupVo;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import com.bocoo.product.service.ProductPriceSettingService;
import com.bocoo.product.service.ProductSaleProductService;
import com.bocoo.product.service.ProductShippingTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerQuoteCatalogServiceImpl implements CustomerQuoteCatalogService {

    private static final Long PLATFORM_TENANT_ID = 1L;

    private final ProductSaleProductService saleProductService;
    private final ProductPriceSettingService priceSettingService;
    private final ProductShippingTemplateService shippingTemplateService;

    @Override
    public List<ProductSaleProductVo> queryProducts(ProductSaleProductBo bo) {
        return TenantContextHolder.callWithTenant(PLATFORM_TENANT_ID, () -> saleProductService.queryList(bo));
    }

    @Override
    public ProductPriceSetupVo querySetup(Long saleProductId) {
        return TenantContextHolder.callWithTenant(PLATFORM_TENANT_ID, () -> priceSettingService.querySetup(saleProductId));
    }

    @Override
    public CustomerQuoteCatalogSetupVo queryCatalogSetup(Long saleProductId) {
        ProductPriceSetupVo setup = querySetup(saleProductId);
        CustomerQuoteCatalogSetupVo result = new CustomerQuoteCatalogSetupVo();
        result.setFormulaOptions(setup.getFormulaOptions());
        result.setFormulaOptionValues(setup.getFormulaOptionValues());
        return result;
    }

    @Override
    public ProductPriceQuoteVo quote(Long saleProductId, ProductPriceQuoteBo bo) {
        return TenantContextHolder.callWithTenant(PLATFORM_TENANT_ID, () -> priceSettingService.quote(saleProductId, bo));
    }

    @Override
    public List<ProductShippingTemplateVo> queryShippingTemplates(ProductShippingTemplateBo bo) {
        return TenantContextHolder.callWithTenant(PLATFORM_TENANT_ID, () -> shippingTemplateService.queryList(bo));
    }

    @Override
    public ProductShippingTemplateVo queryShippingTemplate(Long id) {
        return TenantContextHolder.callWithTenant(PLATFORM_TENANT_ID, () -> shippingTemplateService.queryById(id));
    }
}
