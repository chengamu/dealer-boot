package com.bocoo.pay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditAdjustBo {
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String reason;
}
