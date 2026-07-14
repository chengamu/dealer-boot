package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;

final class QuickOrderQueryCriteria {
    private QuickOrderQueryCriteria() {
    }

    static QueryWrapper<QuickOrder> apply(QueryWrapper<QuickOrder> query, QuickOrderBo bo) {
        if (bo == null) return query;
        return query.eq(bo.getTenantId() != null, "tenant_id", bo.getTenantId())
            .eq(StringUtils.isNotBlank(bo.getBusinessOrigin()), "business_origin", bo.getBusinessOrigin())
            .eq(bo.getSalesStoreId() != null, "sales_store_id", bo.getSalesStoreId())
            .eq(bo.getDeptId() != null, "dept_id", bo.getDeptId())
            .eq(bo.getOwnerUserId() != null, "owner_user_id", bo.getOwnerUserId())
            .like(StringUtils.isNotBlank(bo.getQuickOrderNo()), "quick_order_no", bo.getQuickOrderNo())
            .eq(bo.getCustomerId() != null, "customer_id", bo.getCustomerId())
            .like(StringUtils.isNotBlank(bo.getCustomerName()), "customer_name", bo.getCustomerName())
            .eq(StringUtils.isNotBlank(bo.getStatus()), "status", bo.getStatus())
            .eq(StringUtils.isNotBlank(bo.getCreateBy()), "create_by", bo.getCreateBy())
            .ge(bo.getUpdateTimeStart() != null, "update_time", bo.getUpdateTimeStart())
            .le(bo.getUpdateTimeEnd() != null, "update_time", bo.getUpdateTimeEnd());
    }
}
