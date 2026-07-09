package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 面料条件价格规则 pc_price_fabric_rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_price_fabric_rule")
@Schema(description = "面料条件价格规则")
public class ProductPriceFabricRule extends BaseEntity {

    @TableId(value = "fabric_rule_id")
    private Long fabricRuleId;
    private Long tenantId;
    private Long priceFabricId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private String conditionType;
    private String conditionJson;
    private String conditionExpression;
    private String conditionText;
    private String conditionKey;
    private String priceMode;
    private BigDecimal unitPrice;
    private String priceFormula;
    private Boolean defaultRuleFlag;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
