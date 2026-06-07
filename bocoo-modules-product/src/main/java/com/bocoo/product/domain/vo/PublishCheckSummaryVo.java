package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 发布检查汇总视图对象
 */
@Data
@Schema(description = "发布检查汇总视图对象")
public class PublishCheckSummaryVo {

    @Schema(description = "检查结果：PASS通过，WARNING警告，BLOCKER阻断")
    private String resultStatus = "PASS";

    @Schema(description = "阻断数量")
    private Integer blockerCount = 0;

    @Schema(description = "警告数量")
    private Integer warningCount = 0;

    @Schema(description = "通过数量")
    private Integer passCount = 0;

    @Schema(description = "检查明细")
    private List<PublishCheckResultVo> results = new ArrayList<>();
}
