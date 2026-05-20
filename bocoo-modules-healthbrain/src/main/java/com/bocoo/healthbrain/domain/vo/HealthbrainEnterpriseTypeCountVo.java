package com.bocoo.healthbrain.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 企业类型统计视图对象
 *
 * @author cmx
 */
@Data
@Schema(description = "企业类型统计视图对象")
public class HealthbrainEnterpriseTypeCountVo {

    /**
     * 字典标签
     */
    @Schema(description = "字典标签")
    private String dictLabel;

    /**
     * 数量
     */
    @Schema(description = "数量")
    private Long count;
}