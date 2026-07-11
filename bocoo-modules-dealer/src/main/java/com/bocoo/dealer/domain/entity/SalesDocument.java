package com.bocoo.dealer.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dealer_sales_document")
public class SalesDocument extends BaseEntity {
    @TableId(value = "sales_document_id")
    private Long salesDocumentId;
    private Long tenantId;
    private Long merchantId;
    private String merchantName;
    private String quoteNo;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String companyName;
    private String customerEmail;
    private String customerPhone;
    private Long ownerUserId;
    private String ownerName;
    private String projectName;
    private String customerPoNo;
    private LocalDate validUntil;
    private String recipientName;
    private String recipientPhone;
    private String shippingAddress;
    private String currencyCode;
    private BigDecimal listAmount;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal productAmount;
    private BigDecimal shippingAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private String documentStatus;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal paidAmount;
    private String paymentReference;
    private Long paymentProofMediaId;
    private Long paymentConfirmedById;
    private String paymentConfirmedBy;
    private String productionStatus;
    private String shipmentStatus;
    private LocalDateTime quotedTime;
    private LocalDateTime submittedTime;
    private LocalDateTime paidTime;
    private LocalDateTime productionStartTime;
    private LocalDateTime productionCompleteTime;
    private LocalDateTime shippedTime;
    private LocalDateTime deliveredTime;
    private String carrierName;
    private String trackingNo;
    private String delFlag;
    private String remark;
}
