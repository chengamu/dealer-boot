package com.bocoo.dealer.domain.bo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerQuoteConvertOrderBo {
    @NotBlank(message = "{customer.quote.order.recipient.required}")
    private String recipientName;
    @NotBlank(message = "{customer.quote.order.phone.required}")
    private String recipientPhone;
    @NotBlank(message = "{customer.quote.order.address.required}")
    private String shippingAddress;
    private String customerPoNo;
    private String remark;
    @NotNull(message = "{customer.quote.order.expectedTotal.required}")
    private BigDecimal expectedTotalAmount;
}
