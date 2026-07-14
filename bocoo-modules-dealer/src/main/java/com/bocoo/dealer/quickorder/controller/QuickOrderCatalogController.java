package com.bocoo.dealer.quickorder.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.merchant.domain.vo.CustomerQuoteCatalogSetupVo;
import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/dealer/quick-orders/catalog")
public class QuickOrderCatalogController {

    private final CustomerQuoteCatalogService catalogService;

    @SaCheckPermission("dealer:quick-order:query")
    @GetMapping("/products")
    public R<List<ProductSaleProductVo>> products(ProductSaleProductBo bo) {
        return R.ok(catalogService.queryProducts(bo));
    }

    @SaCheckPermission("dealer:quick-order:query")
    @GetMapping("/products/{saleProductId:\\d+}/setup")
    public R<CustomerQuoteCatalogSetupVo> setup(@PathVariable Long saleProductId) {
        return R.ok(catalogService.queryCatalogSetup(saleProductId));
    }
}
