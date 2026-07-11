package com.bocoo.dealer.service.impl;

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
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SalesCatalogFacade extends DealerServiceSupport {
    private final ProductSaleProductService saleProductService;
    private final ProductPriceSettingService priceService;
    private final ProductShippingTemplateService shippingService;

    public List<ProductSaleProductVo> options() {
        ProductSaleProductBo bo = new ProductSaleProductBo();
        bo.setStatus("ENABLED");
        bo.setPriceStatus("READY");
        return platformProduct(() -> saleProductService.queryList(bo));
    }

    ProductPriceSetupVo setup(Long productId) {
        return platformProduct(() -> priceService.querySetup(productId));
    }

    public com.bocoo.dealer.domain.vo.SalesProductSetupVo publicSetup(Long productId) {
        ProductPriceSetupVo setup = setup(productId);
        com.bocoo.dealer.domain.vo.SalesProductSetupVo vo = new com.bocoo.dealer.domain.vo.SalesProductSetupVo();
        vo.setSaleProduct(setup.getSaleProduct());
        vo.setOptions(setup.getFormulaOptions() == null ? List.of() : setup.getFormulaOptions().stream()
            .filter(row -> Boolean.TRUE.equals(row.getBusinessVisibleFlag())).toList());
        vo.setOptionValues(setup.getFormulaOptionValues());
        return vo;
    }

    ProductPriceQuoteVo quote(Long productId, ProductPriceQuoteBo bo) {
        return platformProduct(() -> priceService.quote(productId, bo));
    }

    ProductShippingTemplateVo shippingTemplate(String currency) {
        ProductShippingTemplateBo bo = new ProductShippingTemplateBo();
        bo.setStatus("ENABLED");
        List<ProductShippingTemplateVo> rows = platformProduct(() -> shippingService.queryList(bo));
        ProductShippingTemplateVo selected = rows.stream()
            .filter(row -> currency.equalsIgnoreCase(row.getCurrencyCode()))
            .min(Comparator.comparing(row -> Boolean.TRUE.equals(row.getDefaultFlag()) ? 0 : 1))
            .orElse(null);
        return selected == null ? null : platformProduct(() -> shippingService.queryById(selected.getShippingTemplateId()));
    }
}
