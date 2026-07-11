package com.bocoo.dealer.domain.vo;

import com.bocoo.dealer.domain.entity.SalesDocument;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AutoMapper(target = SalesDocument.class)
public class SalesDocumentVo implements Serializable {
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
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
    private Integer itemCount;
    private List<SalesDocumentItemVo> items;
    private List<SalesDocumentEventVo> events;
}
