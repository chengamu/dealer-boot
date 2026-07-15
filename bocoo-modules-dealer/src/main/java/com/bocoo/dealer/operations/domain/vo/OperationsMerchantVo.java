package com.bocoo.dealer.operations.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OperationsMerchantVo {

    private String source;
    private Long applicationId;
    private Long merchantId;
    private Long tenantId;
    private String merchantName;
    private String companyName;
    private String contactName;
    private String primaryEmail;
    private String officePhone;
    private String mobilePhone;
    private String country;
    private String state;
    private String city;
    private String addressLine1;
    private String addressLine2;
    private String postalCode;
    private String status;
    private String auditStatus;
    private String auditBy;
    private LocalDateTime auditTime;
    private String rejectReason;
    private String levelName;
    private String levelCode;
    private BigDecimal discountRate;
    private BigDecimal creditLimit;
    private long orderCount;
    private LocalDateTime createTime;
}
