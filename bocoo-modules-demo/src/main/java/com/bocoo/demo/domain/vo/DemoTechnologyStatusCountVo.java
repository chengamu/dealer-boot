package com.bocoo.demo.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 技术管理示例状态统计视图对象。
 */
@Data
@Schema(description = "技术管理示例状态统计视图对象")
public class DemoTechnologyStatusCountVo {

    @Schema(description = "状态")
    private String status;

    @Schema(description = "数量")
    private Long count;
}
