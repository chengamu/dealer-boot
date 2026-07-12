package com.bocoo.pay.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayBankReviewBo {
    @NotNull
    private Boolean approved;
    private String reason;
}
