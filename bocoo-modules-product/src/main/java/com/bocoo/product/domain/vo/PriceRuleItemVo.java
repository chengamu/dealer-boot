package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.PriceRuleItem;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 价格规则项视图对象
 */
@Data
@AutoMapper(target = PriceRuleItem.class)
@Schema(description = "价格规则项视图对象")
public class PriceRuleItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 价格规则项ID
     */
    @Schema(description = "价格规则项ID")
    private Long ruleItemId;

    /**
     * 价格方案版本ID
     */
    @Schema(description = "价格方案版本ID")
    private Long pricePlanVersionId;

    /**
     * 价格方案编码
     */
    @Schema(description = "价格方案编码")
    private String pricePlanCode;

    /**
     * 价格规则项编码
     */
    @Schema(description = "价格规则项编码")
    private String itemCode;

    /**
     * 价格规则项中文名称
     */
    @Schema(description = "价格规则项中文名称")
    private String itemNameCn;

    /**
     * 价格规则项英文名称
     */
    @Schema(description = "价格规则项英文名称")
    private String itemNameEn;

    /**
     * 规则项类型：BASE基础价，MATRIX矩阵价，AREA面积价，OPTION_ADDER选项加价
     */
    @Schema(description = "规则项类型：BASE基础价，MATRIX矩阵价，AREA面积价，OPTION_ADDER选项加价")
    private String itemType;

    /**
     * 匹配条件快照JSON
     */
    @Schema(description = "匹配条件快照JSON")
    private String matchJson;

    /**
     * 计价公式快照JSON
     */
    @Schema(description = "计价公式快照JSON")
    private String formulaJson;

    /**
     * 基础金额
     */
    @Schema(description = "基础金额")
    private BigDecimal baseAmount;

    /**
     * 单位价格
     */
    @Schema(description = "单位价格")
    private BigDecimal unitPrice;

    /**
     * 币种编码
     */
    @Schema(description = "币种编码")
    private String currencyCode;

    /**
     * 优先级
     */
    @Schema(description = "优先级")
    private Integer priority;

    /**
     * 状态：1正常，0停用
     */
    @Schema(description = "状态：1正常，0停用")
    private String status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间，UTC语义
     */
    @Schema(description = "创建时间，UTC语义")
    private LocalDateTime createTime;

    /**
     * 更新时间，UTC语义
     */
    @Schema(description = "更新时间，UTC语义")
    private LocalDateTime updateTime;
}
