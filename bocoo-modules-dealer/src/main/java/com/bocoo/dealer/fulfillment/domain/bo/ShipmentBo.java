package com.bocoo.dealer.fulfillment.domain.bo;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ShipmentBo {
    @Size(max = 64)
    private String packageNo;
    @Size(max = 64)
    private String carrierCode;
    @Size(max = 100)
    private String carrierName;
    @Size(max = 128)
    private String trackingNo;
    private BigDecimal weight;
    @Size(max = 32)
    private String weightUnit;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    @Size(max = 32)
    private String dimensionUnit;
    private Long labelMediaId;
    private Long packingListMediaId;
    @Size(max = 1000)
    private String remark;
    @Valid
    @NotEmpty
    private List<ShipmentItemBo> items;
}
