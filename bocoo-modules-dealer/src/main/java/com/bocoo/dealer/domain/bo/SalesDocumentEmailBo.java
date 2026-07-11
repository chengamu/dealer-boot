package com.bocoo.dealer.domain.bo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SalesDocumentEmailBo {
    @NotBlank
    @Email
    private String recipient;
    private String subject;
    private String message;
    private String documentType;
}
