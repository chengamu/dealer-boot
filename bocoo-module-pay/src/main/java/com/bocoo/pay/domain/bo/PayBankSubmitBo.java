package com.bocoo.pay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayBankSubmitBo {
    @NotBlank
    private String payerName;
    @NotBlank
    private String bankReference;
    @NotNull
    private LocalDateTime transferredTime;
    @NotNull
    private Long declaredPrice;
    @NotBlank
    private String currency;
    @NotNull
    private Long proofMediaId;
    private String remark;
}
