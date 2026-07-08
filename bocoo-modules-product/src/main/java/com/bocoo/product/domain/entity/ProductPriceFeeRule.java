package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 附加费用规则 pc_price_fee_rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_price_fee_rule")
@Schema(description = "附加费用规则")
public class ProductPriceFeeRule extends BaseEntity {

    @TableId(value = "fee_rule_id")
    private Long feeRuleId;
    private Long tenantId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private String feeCode;
    private String feeName;
    private String feeCategory;
    private String triggerCondition;
    private String feeMode;
    private BigDecimal feeAmount;
    private String formulaText;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
