package com.bocoo.product.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 配置求值业务对象
 */
@Data
@Schema(description = "配置求值业务对象")
public class ConfigEvaluationBo {

    @Schema(description = "配置模板版本ID")
    private Long templateVersionId;

    @Schema(description = "用户已选答案JSON对象")
    private Map<String, Object> selectedOptions;

    @Schema(description = "尺寸和输入参数JSON对象")
    private Map<String, Object> inputValues;
}
