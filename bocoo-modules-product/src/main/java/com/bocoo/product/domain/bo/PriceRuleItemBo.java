package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.PriceRuleItem;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 价格规则项业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PriceRuleItem.class, reverseConvertGenerate = false)
@Schema(description = "价格规则项业务对象")
public class PriceRuleItemBo extends BaseBo {

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

}
