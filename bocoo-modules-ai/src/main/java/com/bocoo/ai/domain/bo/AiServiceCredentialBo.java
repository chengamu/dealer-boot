package com.bocoo.ai.domain.bo;

import com.bocoo.ai.domain.entity.AiServiceCredential;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiServiceCredential.class, reverseConvertGenerate = false)
@Schema(description = "AI 服务密钥查询对象")
public class AiServiceCredentialBo extends BaseBo {

    @Schema(description = "服务名")
    private String serviceName;

    @Schema(description = "状态：1启用，0禁用")
    private String status;
}
