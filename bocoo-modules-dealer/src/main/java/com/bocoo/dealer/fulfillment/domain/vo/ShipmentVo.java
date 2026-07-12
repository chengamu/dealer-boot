package com.bocoo.dealer.fulfillment.domain.vo;

import com.bocoo.dealer.fulfillment.domain.entity.Shipment;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AutoMapper(target = Shipment.class)
public class ShipmentVo implements Serializable {
    private Long shipmentId;
    private Long tenantId;
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
    private LocalDateTime shippedTime;
    private String receiptStatus;
    private LocalDateTime receivedTime;
    private LocalDateTime lastTrackingTime;
    private String trackingErrorCode;
    private String trackingErrorMessage;
    private String remark;
    private List<ShipmentItemVo> items;
    private List<TrackingEventVo> trackingEvents;
}
