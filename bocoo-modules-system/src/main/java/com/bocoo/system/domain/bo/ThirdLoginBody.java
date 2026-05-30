package com.bocoo.system.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "第三方登录对象")
public class ThirdLoginBody {

    @Schema(description = "应用唯一标识")
    @NotBlank(message = "{auth.third.credentials.required}")
    private String appKey;

    @Schema(description = "应用密钥")
    @NotBlank(message = "{auth.third.credentials.required}")
    private String secretKey;
}
