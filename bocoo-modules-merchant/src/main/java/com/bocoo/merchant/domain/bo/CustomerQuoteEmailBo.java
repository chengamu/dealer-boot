package com.bocoo.merchant.domain.bo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerQuoteEmailBo {
    @NotBlank(message = "{customer.quote.email.recipient.required}")
    @Email(message = "{customer.quote.email.recipient.invalid}")
    private String recipient;
    private String subject;
    private String message;
}
