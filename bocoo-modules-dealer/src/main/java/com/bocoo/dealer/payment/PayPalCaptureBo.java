package com.bocoo.dealer.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PayPalCaptureBo {
    @NotBlank
    private String paypalOrderId;
}
