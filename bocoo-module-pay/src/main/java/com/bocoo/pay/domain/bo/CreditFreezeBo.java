package com.bocoo.pay.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreditFreezeBo {
    @NotNull
    private Boolean frozen;
    @NotBlank
    private String reason;
}
