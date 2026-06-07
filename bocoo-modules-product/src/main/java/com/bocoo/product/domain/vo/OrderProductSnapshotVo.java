package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 订单产品快照视图对象
 */
@Data
@Schema(description = "订单产品快照视图对象")
public class OrderProductSnapshotVo {

    @Schema(description = "构建状态：BUILT已构建，BLOCKER阻断")
    private String resultStatus = "BUILT";

    @Schema(description = "阻断消息键或文本")
    private List<String> blockers = new ArrayList<>();

    @Schema(description = "产品能力快照实例ID")
    private Long snapshotId;

    @Schema(description = "来源系统：ORDER订单，ERP，MES")
    private String sourceSystem;

    @Schema(description = "来源业务类型：ORDER_LINE订单行，QUOTE_LINE报价行，MES_PLAN生产计划")
    private String sourceBizType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源业务行号")
    private String sourceBizLineNo;

    @Schema(description = "客户编码")
    private String customerCode;

    @Schema(description = "发布包ID")
    private Long packageId;

    @Schema(description = "发布包编码")
    private String packageCode;

    @Schema(description = "发布包哈希")
    private String packageHash;

    @Schema(description = "产品模型编码")
    private String productModelCode;

    @Schema(description = "销售变体编码")
    private String salesVariantCode;

    @Schema(description = "配置模板版本ID")
    private Long templateVersionId;

    @Schema(description = "配置模板版本号")
    private String templateVersionNo;

    @Schema(description = "价格方案版本ID")
    private Long pricePlanVersionId;

    @Schema(description = "价格方案编码")
    private String pricePlanCode;

    @Schema(description = "用户已选答案JSON对象")
    private Map<String, Object> selectedOptions;

    @Schema(description = "尺寸和输入参数JSON对象")
    private Map<String, Object> inputValues;

    @Schema(description = "订单产品快照JSON")
    private String snapshotJson;

    @Schema(description = "订单产品快照哈希")
    private String snapshotHash;

    @Schema(description = "快照构建时间，UTC语义")
    private LocalDateTime builtTime;
}
