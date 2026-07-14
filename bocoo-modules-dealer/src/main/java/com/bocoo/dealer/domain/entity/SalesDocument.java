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
    private String businessOrigin;
    private Long salesStoreId;
    private Long deptId;
    private Long merchantId;
    private String merchantName;
    private String sourceType;
    private Long sourceQuoteId;
    private Long sourceQuickOrderId;
    private String sourceNo;
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
    private Long payOrderId;
    private String payOrderNo;
    private String documentStatus;
    private String paymentStatus;
    private String paymentMethod;
    private BigDecimal paidAmount;
    private String paymentReference;
    private Long paymentProofMediaId;
    private Long paymentConfirmedById;
    private String paymentConfirmedBy;
    private String productionStatus;
    private Long productionStartedById;
    private String productionStartedBy;
    private String shipmentStatus;
    private Integer shipmentCount;
    private Integer shippedQuantity;
    private LocalDateTime submittedTime;
    private LocalDateTime paidTime;
    private LocalDateTime productionStartTime;
    private Long productionCompletedById;
    private String productionCompletedBy;
    private LocalDateTime productionCompleteTime;
    private String productionRemark;
    private LocalDateTime firstShippedTime;
    private LocalDateTime latestShippedTime;
    private String latestCarrierName;
    private String latestTrackingNo;
    private LocalDateTime shippedTime;
    private LocalDateTime deliveredTime;
    private LocalDateTime completedTime;
    private String carrierName;
    private String trackingNo;
    private Long cancelledById;
    private String cancelledBy;
    private LocalDateTime cancelledTime;
    private String cancelReason;
    private String delFlag;
    private String remark;
}
