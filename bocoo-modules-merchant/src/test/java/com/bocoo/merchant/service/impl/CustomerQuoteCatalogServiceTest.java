package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import com.bocoo.product.service.ProductPriceSettingService;
import com.bocoo.product.service.ProductSaleProductService;
import com.bocoo.product.service.ProductShippingTemplateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteCatalogServiceTest {

    @Mock
    private ProductSaleProductService saleProductService;
    @Mock
    private ProductPriceSettingService priceSettingService;
    @Mock
    private ProductShippingTemplateService shippingTemplateService;

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void merchantReadsProductsFromPlatformTenantAndRestoresContext() {
        TenantContextHolder.setTenantId(200L);
        ProductSaleProductVo product = new ProductSaleProductVo();
        when(saleProductService.queryList(any())).thenAnswer(invocation -> {
            assertThat(TenantContextHolder.getTenantId()).isEqualTo(1L);
            return List.of(product);
        });
        CustomerQuoteCatalogServiceImpl service = new CustomerQuoteCatalogServiceImpl(
            saleProductService, priceSettingService, shippingTemplateService);

        assertThat(service.queryProducts(new ProductSaleProductBo())).containsExactly(product);
        assertThat(TenantContextHolder.getTenantId()).isEqualTo(200L);
    }
}
