package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品能力工作台优先队列视图对象
 */
@Data
@Schema(description = "产品能力工作台优先队列视图对象")
public class WorkbenchPriorityVo {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private Long taskId;

    /**
     * 任务类型
     */
    @Schema(description = "任务类型")
    private String taskType;

    /**
     * 严重等级
     */
    @Schema(description = "严重等级")
    private String severity;

    /**
     * 目标类型
     */
    @Schema(description = "目标类型")
    private String targetType;

    /**
     * 目标编码
     */
    @Schema(description = "目标编码")
    private String targetCode;

    /**
     * 目标名称
     */
    @Schema(description = "目标名称")
    private String targetName;

    /**
     * 负责人名称
     */
    @Schema(description = "负责人名称")
    private String ownerName;

    /**
     * 截止时间，UTC语义
     */
    @Schema(description = "截止时间，UTC语义")
    private LocalDateTime dueTime;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

}
