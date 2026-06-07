package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品能力工作台汇总视图对象
 */
@Data
@Schema(description = "产品能力工作台汇总视图对象")
public class WorkbenchSummaryVo {

    /**
     * 产品模型数量
     */
    @Schema(description = "产品模型数量")
    private Long modelCount = 0L;

    /**
     * 草稿数量
     */
    @Schema(description = "草稿数量")
    private Long draftCount = 0L;

    /**
     * 已发布数量
     */
    @Schema(description = "已发布数量")
    private Long publishedCount = 0L;

    /**
     * 阻断问题数量
     */
    @Schema(description = "阻断问题数量")
    private Long blockerCount = 0L;

    /**
     * 警告问题数量
     */
    @Schema(description = "警告问题数量")
    private Long warningCount = 0L;

    /**
     * 待同步数量
     */
    @Schema(description = "待同步数量")
    private Long pendingSyncCount = 0L;

    /**
     * 最近同步时间，UTC语义
     */
    @Schema(description = "最近同步时间，UTC语义")
    private LocalDateTime lastSyncTime;

}
