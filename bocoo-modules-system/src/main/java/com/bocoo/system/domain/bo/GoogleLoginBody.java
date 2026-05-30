package com.bocoo.system.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "Google 登录对象")
public class GoogleLoginBody {

    @Schema(description = "Google Identity Services ID token")
    @NotBlank(message = "{auth.google.credential.required}")
    private String credential;
}
