package com.bocoo.pay.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.pay.api.PaymentDocumentScopeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class PaymentDocumentScopeSupport {
    private final ObjectProvider<PaymentDocumentScopeResolver> resolvers;

    PaymentDocumentScopeResolver required() {
        PaymentDocumentScopeResolver resolver = resolvers.getIfUnique();
        if (resolver == null) {
            throw new ServiceException("Sales document payment scope resolver is unavailable");
        }
        return resolver;
    }
}
