package com.bocoo.ai.domain.bo;

import com.bocoo.ai.domain.entity.AiProviderConfig;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiProviderConfig.class, reverseConvertGenerate = false)
@Schema(description = "AI Provider 查询对象")
public class AiProviderQueryBo extends BaseBo {

    @Schema(description = "Provider 编码")
    private String providerCode;

    @Schema(description = "Provider 名称")
    private String providerName;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "状态：1启用，0禁用")
    private String status;
}
