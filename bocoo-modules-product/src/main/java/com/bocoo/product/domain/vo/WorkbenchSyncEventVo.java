package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 产品能力工作台同步事件视图对象
 */
@Data
@Schema(description = "产品能力工作台同步事件视图对象")
public class WorkbenchSyncEventVo {

    /**
     * 同步事件ID
     */
    @Schema(description = "同步事件ID")
    private Long eventId;

    /**
     * 事件类型
     */
    @Schema(description = "事件类型")
    private String eventType;

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
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 重试次数
     */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /**
     * 最近错误消息键
     */
    @Schema(description = "最近错误消息键")
    private String lastErrorKey;

    /**
     * 创建时间，UTC语义
     */
    @Schema(description = "创建时间，UTC语义")
    private LocalDateTime createdTime;

    /**
     * 更新时间，UTC语义
     */
    @Schema(description = "更新时间，UTC语义")
    private LocalDateTime updatedTime;

}
