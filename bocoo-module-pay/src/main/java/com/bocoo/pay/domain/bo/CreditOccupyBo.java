package com.bocoo.pay.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditOccupyBo {
    @NotNull
    private Long payOrderId;
    @NotNull
    private Long salesDocumentId;
    @NotNull
    private Long merchantId;
    private String merchantName;
    @NotNull
    private BigDecimal configuredCreditLimit;
    @NotNull
    private Integer creditTermDays;
}
