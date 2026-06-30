package com.bocoo.ai.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "AI 服务密钥生成对象")
public class AiServiceCredentialGenerateBo extends BaseBo {
    @Schema(description = "服务名")
    private String serviceName = "ai-runtime";

    @Schema(description = "备注")
    private String remark;
}
