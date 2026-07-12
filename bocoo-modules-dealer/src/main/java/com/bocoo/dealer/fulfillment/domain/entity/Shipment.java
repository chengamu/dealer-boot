package com.bocoo.dealer.fulfillment.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dealer_shipment")
public class Shipment extends BaseEntity {
    @TableId(value = "shipment_id")
    private Long shipmentId;
    private Long tenantId;
    private Long merchantId;
    private String merchantName;
    private Long salesDocumentId;
    private String shipmentNo;
    private String packageNo;
    private String carrierCode;
    private String carrierName;
    private String trackingNo;
    private String status;
    private String trackingStatus;
    private Integer itemQuantity;
    private BigDecimal weight;
    private String weightUnit;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private String dimensionUnit;
    private Long labelMediaId;
    private Long packingListMediaId;
    private Long shippedById;
    private String shippedBy;
    private LocalDateTime shippedTime;
    private String receiptStatus;
    private Long receivedById;
    private String receivedBy;
    private LocalDateTime receivedTime;
    private String receiptOverrideReason;
    private LocalDateTime lastTrackingTime;
    private String trackingErrorCode;
    private String trackingErrorMessage;
    private String delFlag;
    private String remark;
}
