package com.bocoo.ai.domain.bo;

import com.bocoo.ai.domain.entity.AiProviderModel;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiProviderModel.class, reverseConvertGenerate = false)
@Schema(description = "AI Provider 模型查询对象")
public class AiProviderModelQueryBo extends BaseBo {

    @Schema(description = "Provider 编码")
    private String providerCode;

    @Schema(description = "模型编码")
    private String modelCode;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "模型类型")
    private String modelType;

    @Schema(description = "状态：1启用，0禁用")
    private String status;
}
