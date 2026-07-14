package com.bocoo.dealer.fulfillment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.domain.entity.SalesDocument;

public record FulfillmentBusinessScope(Long tenantId, String businessOrigin,
                                       Long deptId, Long ownerUserId) {
    public void apply(QueryWrapper<SalesDocument> query) {
        query.eq("tenant_id", tenantId).eq("business_origin", businessOrigin)
            .eq(deptId != null, "dept_id", deptId)
            .eq(ownerUserId != null, "owner_user_id", ownerUserId);
    }
}
