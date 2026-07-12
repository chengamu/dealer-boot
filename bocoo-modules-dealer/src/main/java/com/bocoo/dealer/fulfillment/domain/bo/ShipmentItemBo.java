package com.bocoo.dealer.fulfillment.domain.bo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShipmentItemBo {
    @NotNull
    private Long salesItemId;
    @NotNull
    @Min(1)
    private Integer quantity;
    @Size(max = 1000)
    private String remark;
}
