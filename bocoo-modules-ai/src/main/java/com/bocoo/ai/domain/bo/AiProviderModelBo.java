package com.bocoo.ai.domain.bo;

import com.bocoo.ai.domain.entity.AiProviderModel;
import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = AiProviderModel.class, reverseConvertGenerate = false)
@Schema(description = "AI Provider 模型对象")
public class AiProviderModelBo extends BaseBo {
    @Schema(description = "模型ID")
    private Long modelId;

    @NotBlank(message = "ai.provider.code.required")
    @Schema(description = "Provider 编码")
    private String providerCode;

    @NotBlank(message = "ai.model.code.required")
    @Schema(description = "模型编码")
    private String modelCode;

    @NotBlank(message = "ai.model.name.required")
    @Schema(description = "模型名称")
    private String modelName;

    @NotBlank(message = "ai.model.type.required")
    @Schema(description = "模型类型")
    private String modelType;

    @Schema(description = "上下文窗口")
    private Integer contextWindow;

    @Schema(description = "输入价格")
    private BigDecimal inputPrice;

    @Schema(description = "输出价格")
    private BigDecimal outputPrice;

    @Schema(description = "是否默认模型")
    private Boolean defaultModel;

    @Schema(description = "状态：1启用，0禁用")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
