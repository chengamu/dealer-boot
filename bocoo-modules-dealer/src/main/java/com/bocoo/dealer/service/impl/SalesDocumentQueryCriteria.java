package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.entity.SalesDocument;

final class SalesDocumentQueryCriteria {
    private SalesDocumentQueryCriteria() {
    }

    static QueryWrapper<SalesDocument> apply(QueryWrapper<SalesDocument> query, SalesDocumentBo bo) {
        if (bo == null) return query;
        return query.eq(bo.getTenantId() != null, "tenant_id", bo.getTenantId())
            .eq(StringUtils.isNotBlank(bo.getBusinessOrigin()), "business_origin", bo.getBusinessOrigin())
            .eq(bo.getSalesStoreId() != null, "sales_store_id", bo.getSalesStoreId())
            .eq(bo.getDeptId() != null, "dept_id", bo.getDeptId())
            .eq(StringUtils.isNotBlank(bo.getSourceType()), "source_type", bo.getSourceType())
            .like(StringUtils.isNotBlank(bo.getSourceNo()), "source_no", bo.getSourceNo())
            .like(StringUtils.isNotBlank(bo.getQuoteNo()), "quote_no", bo.getQuoteNo())
            .like(StringUtils.isNotBlank(bo.getOrderNo()), "order_no", bo.getOrderNo())
            .eq(bo.getCustomerId() != null, "customer_id", bo.getCustomerId())
            .like(StringUtils.isNotBlank(bo.getCustomerName()), "customer_name", bo.getCustomerName())
            .like(StringUtils.isNotBlank(bo.getMerchantName()), "merchant_name", bo.getMerchantName())
            .eq(StringUtils.isNotBlank(bo.getDocumentStatus()), "document_status", bo.getDocumentStatus())
            .eq(StringUtils.isNotBlank(bo.getPaymentStatus()), "payment_status", bo.getPaymentStatus())
            .eq(StringUtils.isNotBlank(bo.getProductionStatus()), "production_status", bo.getProductionStatus())
            .eq(StringUtils.isNotBlank(bo.getShipmentStatus()), "shipment_status", bo.getShipmentStatus())
            .eq(bo.getOwnerUserId() != null, "owner_user_id", bo.getOwnerUserId());
    }
}
