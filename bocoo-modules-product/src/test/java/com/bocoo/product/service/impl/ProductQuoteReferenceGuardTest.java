package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.service.ProductQuoteReferenceProvider;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductQuoteReferenceGuardTest {

    @Test
    void aggregatesSaleProductReferences() {
        ProductQuoteReferenceProvider first = mock(ProductQuoteReferenceProvider.class);
        ProductQuoteReferenceProvider second = mock(ProductQuoteReferenceProvider.class);
        when(first.countSaleProductReferences(10L)).thenReturn(1L);
        when(second.countSaleProductReferences(10L)).thenReturn(2L);

        ProductQuoteReferenceGuard guard = new ProductQuoteReferenceGuard(List.of(first, second));

        assertThat(guard.countSaleProductReferences(10L)).isEqualTo(3L);
    }

    @Test
    void shippingTemplateReferenceBlocksDelete() {
        ProductQuoteReferenceProvider provider = mock(ProductQuoteReferenceProvider.class);
        when(provider.countShippingTemplateReferences(20L)).thenReturn(1L);
        ProductQuoteReferenceGuard guard = new ProductQuoteReferenceGuard(List.of(provider));

        assertThatThrownBy(() -> guard.assertNoShippingTemplateReferences(20L))
            .isInstanceOf(ServiceException.class);
    }
}
