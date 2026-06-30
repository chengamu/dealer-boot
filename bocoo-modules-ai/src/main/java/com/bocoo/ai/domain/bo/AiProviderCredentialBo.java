package com.bocoo.ai.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI Provider Key 对象")
public class AiProviderCredentialBo extends BaseBo {
    @NotBlank(message = "ai.provider.code.required")
    @Schema(description = "Provider 编码")
    private String providerCode;

    @NotBlank(message = "ai.provider.apiKey.required")
    @Schema(description = "Provider API Key")
    private String apiKey;

    @Schema(description = "备注")
    private String remark;
}
