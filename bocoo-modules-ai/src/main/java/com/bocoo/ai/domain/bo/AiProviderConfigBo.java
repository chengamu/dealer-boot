package com.bocoo.ai.domain.bo;

import com.bocoo.ai.domain.entity.AiProviderConfig;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiProviderConfig.class, reverseConvertGenerate = false)
@Schema(description = "AI Provider 配置对象")
public class AiProviderConfigBo extends BaseBo {
    @Schema(description = "Provider ID")
    private Long providerId;

    @NotBlank(message = "ai.provider.code.required")
    @Schema(description = "Provider 编码")
    private String providerCode;

    @NotBlank(message = "ai.provider.name.required")
    @Schema(description = "Provider 名称")
    private String providerName;

    @NotBlank(message = "ai.provider.baseUrl.required")
    @Schema(description = "Base URL")
    private String baseUrl;

    @Schema(description = "Chat completions 路径")
    private String chatCompletionsPath;

    @Schema(description = "默认模型")
    private String defaultModel;

    @NotNull(message = "ai.provider.timeout.required")
    @Schema(description = "超时时间，秒")
    private Integer timeoutSeconds;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "状态：1启用，0禁用")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
