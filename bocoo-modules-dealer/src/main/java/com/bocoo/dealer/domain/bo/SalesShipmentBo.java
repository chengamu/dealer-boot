package com.bocoo.dealer.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SalesShipmentBo {
    @NotBlank
    private String carrierName;
    @NotBlank
    private String trackingNo;
    private String note;
}
