package com.bocoo.system.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Merchant application email verification code request")
public class MerchantEmailCodeBo {

    @NotBlank(message = "{tenant.apply.email.required}")
    @Email(message = "{tenant.apply.email.invalid}")
    @Size(max = 100, message = "{tenant.apply.email.max}")
    private String email;
}
