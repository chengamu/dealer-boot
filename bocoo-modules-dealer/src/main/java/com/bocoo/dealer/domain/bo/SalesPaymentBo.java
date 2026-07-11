package com.bocoo.dealer.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesPaymentBo {
    @NotBlank
    private String paymentMethod;
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal paidAmount;
    @NotBlank
    private String paymentReference;
    private Long paymentProofMediaId;
    private String note;
}
