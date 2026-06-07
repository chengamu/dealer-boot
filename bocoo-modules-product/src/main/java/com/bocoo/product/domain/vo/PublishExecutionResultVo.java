package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 发布执行结果视图对象
 */
@Data
@Schema(description = "发布执行结果视图对象")
public class PublishExecutionResultVo {

    @Schema(description = "执行结果：PUBLISHED已发布，BLOCKER阻断")
    private String resultStatus;

    @Schema(description = "发布检查汇总")
    private PublishCheckSummaryVo checkSummary;

    @Schema(description = "发布包")
    private ProductPublishPackageVo packageInfo;

    @Schema(description = "同步Outbox")
    private ProductSyncOutboxVo outbox;

    @Schema(description = "同步Outbox列表")
    private List<ProductSyncOutboxVo> outboxes;
}
