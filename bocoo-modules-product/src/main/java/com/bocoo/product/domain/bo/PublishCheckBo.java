package com.bocoo.product.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发布检查业务对象
 */
@Data
@Schema(description = "发布检查业务对象")
public class PublishCheckBo {

    @Schema(description = "产品模型ID")
    private Long productModelId;

    @Schema(description = "产品模型编码")
    private String productModelCode;

    @Schema(description = "销售变体ID")
    private Long salesVariantId;

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
}
