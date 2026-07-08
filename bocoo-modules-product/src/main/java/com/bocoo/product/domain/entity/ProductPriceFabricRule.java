package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 面料价格规则 pc_price_fabric_rule
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_price_fabric_rule")
@Schema(description = "面料价格规则")
public class ProductPriceFabricRule extends BaseEntity {

    @TableId(value = "fabric_rule_id")
    private Long fabricRuleId;
    private Long tenantId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String unitCode;
    private String optionCombinationKey;
    private String optionCombinationName;
    private String priceMode;
    private BigDecimal basePrice;
    private String areaFormula;
    private BigDecimal minBillArea;
    private BigDecimal lossRate;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
