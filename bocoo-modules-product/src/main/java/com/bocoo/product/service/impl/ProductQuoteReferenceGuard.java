package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.service.ProductQuoteReferenceProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.ToLongFunction;

@Component
@RequiredArgsConstructor
public class ProductQuoteReferenceGuard {

    private final List<ProductQuoteReferenceProvider> providers;

    long countSaleProductReferences(Long id) {
        return count(provider -> provider.countSaleProductReferences(id));
    }

    long countFormulaVersionReferences(Long id) {
        return count(provider -> provider.countFormulaVersionReferences(id));
    }

    void assertNoShippingTemplateReferences(Long id) {
        if (count(provider -> provider.countShippingTemplateReferences(id)) > 0) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.quoteReferenced");
        }
    }

    private long count(ToLongFunction<ProductQuoteReferenceProvider> counter) {
        return providers.stream().mapToLong(counter).sum();
    }
}
