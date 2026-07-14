package com.bocoo.pay.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreditRepayBo {
    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;
    @NotNull
    private LocalDateTime paidTime;
    @NotBlank
    private String method;
    @NotBlank
    private String reference;
    private Long proofMediaId;
    @NotBlank
    private String reason;
    @NotBlank
    private String idempotencyKey;
}
