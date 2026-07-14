package com.bocoo.pay.domain.credit;

import com.bocoo.common.core.exception.ServiceException;

public record CreditSubject(CreditSubjectType type, Long tenantId, Long salesStoreId,
                            Long merchantId, String subjectName, String currency) {
    public CreditSubject {
        if (type == null || tenantId == null || currency == null || currency.isBlank()) {
            throw new ServiceException("Credit subject and currency are required");
        }
        if (type == CreditSubjectType.MERCHANT && salesStoreId != null) {
            throw new ServiceException("Merchant credit subject cannot have a sales store");
        }
        if (type == CreditSubjectType.INTERNAL && salesStoreId == null) {
            throw new ServiceException("Internal credit subject requires a sales store");
        }
    }

    public static CreditSubject merchant(Long tenantId, Long merchantId, String name, String currency) {
        return new CreditSubject(CreditSubjectType.MERCHANT, tenantId, null, merchantId, name, currency);
    }

    public static CreditSubject internal(Long tenantId, Long salesStoreId, String name, String currency) {
        return new CreditSubject(CreditSubjectType.INTERNAL, tenantId, salesStoreId, null, name, currency);
    }
}
