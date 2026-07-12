package com.bocoo.pay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaySupplementBo {
    @NotBlank
    private String method;
    @NotNull
    private Long price;
    @NotBlank
    private String currency;
    @NotBlank
    private String reference;
    @NotNull
    private LocalDateTime paidTime;
    @NotNull
    private Long proofMediaId;
    @NotBlank
    private String reason;
}
