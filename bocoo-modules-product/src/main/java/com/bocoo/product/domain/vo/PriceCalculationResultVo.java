package com.bocoo.product.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 价格试算结果视图对象
 */
@Data
@Schema(description = "价格试算结果视图对象")
public class PriceCalculationResultVo {

    @Schema(description = "价格方案版本ID")
    private Long pricePlanVersionId;

    @Schema(description = "试算状态：PRICED已计价，WARNING警告，BLOCKER阻断")
    private String resultStatus = "PRICED";

    @Schema(description = "币种编码")
    private String currencyCode;

    @Schema(description = "基础金额")
    private BigDecimal baseAmount = BigDecimal.ZERO;

    @Schema(description = "选项加价金额")
    private BigDecimal optionAmount = BigDecimal.ZERO;

    @Schema(description = "总金额")
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Schema(description = "命中的价格规则项")
    private List<PriceRuleItemVo> matchedItems = new ArrayList<>();

    @Schema(description = "价格明细")
    private List<Map<String, Object>> breakdown = new ArrayList<>();

    @Schema(description = "警告消息键或文本")
    private List<String> warnings = new ArrayList<>();

    @Schema(description = "阻断消息键或文本")
    private List<String> blockers = new ArrayList<>();
}
