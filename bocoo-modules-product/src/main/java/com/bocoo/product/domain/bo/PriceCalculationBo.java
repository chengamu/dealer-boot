package com.bocoo.product.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 价格试算业务对象
 */
@Data
@Schema(description = "价格试算业务对象")
public class PriceCalculationBo {

    @Schema(description = "价格方案版本ID")
    private Long pricePlanVersionId;

    @Schema(description = "产品模型编码")
    private String productModelCode;

    @Schema(description = "销售变体编码")
    private String salesVariantCode;

    @Schema(description = "币种编码")
    private String currencyCode;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "宽度")
    private BigDecimal width;

    @Schema(description = "高度")
    private BigDecimal height;

    @Schema(description = "用户已选答案JSON对象")
    private Map<String, Object> selectedOptions;

    @Schema(description = "尺寸和输入参数JSON对象")
    private Map<String, Object> inputValues;
}
