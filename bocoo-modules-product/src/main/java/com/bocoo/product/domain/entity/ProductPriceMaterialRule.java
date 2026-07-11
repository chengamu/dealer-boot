package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 配方版本物料条件价格规则。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_price_material_rule")
@Schema(description = "物料条件价格规则")
public class ProductPriceMaterialRule extends BaseEntity {

    @TableId(value = "material_rule_id")
    private Long materialRuleId;
    private Long tenantId;
    private Long priceMaterialId;
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
