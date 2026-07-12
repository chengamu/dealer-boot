package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.service.CustomerQuoteCatalogService;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import com.bocoo.product.service.ProductPriceRuntimeContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerQuotePricingSessionTest {

    @Test
    void reusesRuntimeForTwentyRowsOfTheSameProduct() {
        CustomerQuoteCatalogService catalog = mock(CustomerQuoteCatalogService.class);
        ProductPriceRuntimeContext runtime = runtime(10L, 30L, "USD");
        when(catalog.prepareRuntime(10L)).thenReturn(runtime);
        CustomerQuotePricingSession session = new CustomerQuotePricingSession(200L, catalog);

        for (int index = 0; index < 20; index++) {
            assertThat(session.pricing(10L)).isSameAs(runtime);
        }

        verify(catalog, times(1)).prepareRuntime(10L);
    }

    @Test
    void loadsDifferentProductsIndependently() {
        CustomerQuoteCatalogService catalog = mock(CustomerQuoteCatalogService.class);
        ProductPriceRuntimeContext first = runtime(10L, 30L, "USD");
        ProductPriceRuntimeContext second = runtime(11L, 31L, "USD");
        when(catalog.prepareRuntime(10L)).thenReturn(first);
        when(catalog.prepareRuntime(11L)).thenReturn(second);
        CustomerQuotePricingSession session = new CustomerQuotePricingSession(200L, catalog);

        session.pricing(10L);
        session.pricing(11L);
        session.pricing(10L);

        verify(catalog, times(1)).prepareRuntime(10L);
        verify(catalog, times(1)).prepareRuntime(11L);
    }

    @Test
    void reusesEnabledShippingTemplateForTheCurrency() {
        CustomerQuoteCatalogService catalog = mock(CustomerQuoteCatalogService.class);
        ProductShippingTemplateVo option = new ProductShippingTemplateVo();
        option.setShippingTemplateId(5L);
        ProductShippingTemplateVo detail = new ProductShippingTemplateVo();
        detail.setShippingTemplateId(5L);
        when(catalog.queryShippingTemplates(any(ProductShippingTemplateBo.class))).thenReturn(List.of(option));
        when(catalog.queryShippingTemplate(5L)).thenReturn(detail);
        CustomerQuotePricingSession session = new CustomerQuotePricingSession(200L, catalog);

        assertThat(session.shipping("USD")).isSameAs(detail);
        assertThat(session.shipping("USD")).isSameAs(detail);

        verify(catalog, times(1)).queryShippingTemplates(any(ProductShippingTemplateBo.class));
        verify(catalog, times(1)).queryShippingTemplate(5L);
    }

    @Test
    void cachesMissingShippingTemplateForTheCurrency() {
        CustomerQuoteCatalogService catalog = mock(CustomerQuoteCatalogService.class);
        when(catalog.queryShippingTemplates(any(ProductShippingTemplateBo.class))).thenReturn(List.of());
        CustomerQuotePricingSession session = new CustomerQuotePricingSession(200L, catalog);

        assertThat(session.shipping("USD")).isNull();
        assertThat(session.shipping("USD")).isNull();

        verify(catalog, times(1)).queryShippingTemplates(any(ProductShippingTemplateBo.class));
    }

    private ProductPriceRuntimeContext runtime(Long productId, Long versionId, String currency) {
        ProductPriceRuntimeContext runtime = mock(ProductPriceRuntimeContext.class);
        when(runtime.saleProductId()).thenReturn(productId);
        when(runtime.formulaVersionId()).thenReturn(versionId);
        when(runtime.currencyCode()).thenReturn(currency);
        return runtime;
    }
}
