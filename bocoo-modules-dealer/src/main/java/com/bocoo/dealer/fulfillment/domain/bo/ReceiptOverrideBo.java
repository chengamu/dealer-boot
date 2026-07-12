package com.bocoo.dealer.fulfillment.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReceiptOverrideBo {
    @NotBlank
    @Size(max = 1000)
    private String reason;
}
