package com.bocoo.pay.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.pay.api.CreditSubjectScopeResolver;
import com.bocoo.pay.api.PaymentDocumentFacts;
import com.bocoo.pay.domain.bo.CreditOccupyBo;
import com.bocoo.pay.domain.credit.CreditSubject;
import com.bocoo.pay.domain.credit.CreditSubjectType;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class CreditSubjectSupport {
    private final PaymentDocumentScopeSupport documentScopes;
    private final ObjectProvider<CreditSubjectScopeResolver> currentResolvers;

    CreditSubject forDocument(Long salesDocumentId, String currency, CreditOccupyBo bo) {
        PaymentDocumentFacts facts = documentScopes.required().requireAccessible(salesDocumentId);
        if (CreditSubjectType.INTERNAL.name().equals(facts.getBusinessOrigin())) {
            return CreditSubject.internal(facts.getTenantId(), facts.getSalesStoreId(), facts.getSubjectName(), currency);
        }
        if (!CreditSubjectType.MERCHANT.name().equals(facts.getBusinessOrigin())) {
            throw new ServiceException("Unsupported credit business origin");
        }
        return CreditSubject.merchant(facts.getTenantId(), bo.getMerchantId(), facts.getSubjectName(), currency);
    }

    CreditSubject current(String currency) {
        CreditSubjectScopeResolver resolver = currentResolvers.getIfUnique();
        if (resolver == null) throw new ServiceException("Current credit subject resolver is unavailable");
        return resolver.current(currency);
    }

    CreditSubject from(MerchantCreditAccount account) {
        CreditSubjectType type = CreditSubjectType.valueOf(account.getBusinessOrigin());
        return new CreditSubject(type, account.getTenantId(), account.getSalesStoreId(), account.getMerchantId(),
            account.getMerchantName(), account.getCurrency());
    }
}
