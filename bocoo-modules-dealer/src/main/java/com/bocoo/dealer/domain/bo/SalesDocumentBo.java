package com.bocoo.dealer.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SalesDocumentBo extends BaseEntity {
    private Long salesDocumentId;
    private Long tenantId;
    private String businessOrigin;
    private Long salesStoreId;
    private Long deptId;
    private String sourceType;
    private String sourceNo;
    private String quoteNo;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String projectName;
    private String documentStatus;
    private String paymentStatus;
    private String productionStatus;
    private String shipmentStatus;
    private String merchantName;
    private Long ownerUserId;
}
