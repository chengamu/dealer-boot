package com.bocoo.dealer.scope;

import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.merchant.domain.entity.CustomerQuote;

public record SalesOwnership(Long tenantId, String businessOrigin, Long salesStoreId,
                             Long deptId, Long ownerUserId) {

    public static SalesOwnership from(QuickOrder source) {
        return new SalesOwnership(source.getTenantId(), source.getBusinessOrigin(), source.getSalesStoreId(),
            source.getDeptId(), source.getOwnerUserId());
    }

    public static SalesOwnership from(CustomerQuote source) {
        return new SalesOwnership(source.getTenantId(), source.getBusinessOrigin(), source.getSalesStoreId(),
            source.getDeptId(), source.getOwnerUserId());
    }

    public void applyTo(QuickOrder target) {
        target.setTenantId(tenantId);
        target.setBusinessOrigin(businessOrigin);
        target.setSalesStoreId(salesStoreId);
        target.setDeptId(deptId);
        target.setOwnerUserId(ownerUserId);
    }

    public void applyTo(SalesDocument target) {
        target.setTenantId(tenantId);
        target.setBusinessOrigin(businessOrigin);
        target.setSalesStoreId(salesStoreId);
        target.setDeptId(deptId);
        target.setOwnerUserId(ownerUserId);
    }
}
