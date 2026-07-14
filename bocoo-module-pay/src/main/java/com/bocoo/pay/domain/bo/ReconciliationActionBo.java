package com.bocoo.pay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReconciliationActionBo {
    @NotBlank
    private String reason;
}
