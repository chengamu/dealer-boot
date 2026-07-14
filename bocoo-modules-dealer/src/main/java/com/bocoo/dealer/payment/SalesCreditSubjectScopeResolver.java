package com.bocoo.dealer.payment;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.scope.SalesOwnership;
import com.bocoo.dealer.scope.SalesOwnershipResolver;
import com.bocoo.pay.api.CreditSubjectScopeResolver;
import com.bocoo.pay.domain.credit.CreditSubject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SalesCreditSubjectScopeResolver implements CreditSubjectScopeResolver {
    private final SalesOwnershipResolver ownershipResolver;

    @Override
    public CreditSubject current(String currency) {
        SalesOwnership ownership = ownershipResolver.current();
        if ("MERCHANT".equals(ownership.businessOrigin())) {
            return CreditSubject.merchant(ownership.tenantId(), null, LoginHelper.getUsername(), currency);
        }
        if ("INTERNAL".equals(ownership.businessOrigin())) {
            return CreditSubject.internal(ownership.tenantId(), ownership.salesStoreId(),
                LoginHelper.getUsername(), currency);
        }
        throw new ServiceException("Unsupported credit business origin");
    }
}
