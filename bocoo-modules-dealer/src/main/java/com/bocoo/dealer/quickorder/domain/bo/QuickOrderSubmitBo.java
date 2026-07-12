package com.bocoo.dealer.quickorder.domain.bo;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuickOrderSubmitBo {
    @NotNull(message = "{dealer.quickOrder.expectedTotal.required}")
    private BigDecimal expectedTotalAmount;
}
