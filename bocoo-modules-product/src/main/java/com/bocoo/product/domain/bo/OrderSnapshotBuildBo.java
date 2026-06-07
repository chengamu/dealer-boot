package com.bocoo.product.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 订单产品快照构建业务对象
 */
@Data
@Schema(description = "订单产品快照构建业务对象")
public class OrderSnapshotBuildBo {

    @Schema(description = "发布包ID")
    private Long packageId;

    @Schema(description = "发布包编码")
    private String packageCode;

    @Schema(description = "来源系统：ORDER订单，ERP，MES")
    private String sourceSystem;

    @Schema(description = "来源业务类型：ORDER_LINE订单行，QUOTE_LINE报价行，MES_PLAN生产计划")
    private String sourceBizType;

    @Schema(description = "来源业务单号")
    private String sourceBizNo;

    @Schema(description = "来源业务行号")
    private String sourceBizLineNo;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "客户编码")
    private String customerCode;

    @Schema(description = "用户已选答案JSON对象")
    private Map<String, Object> selectedOptions;

    @Schema(description = "尺寸和输入参数JSON对象")
    private Map<String, Object> inputValues;
}
