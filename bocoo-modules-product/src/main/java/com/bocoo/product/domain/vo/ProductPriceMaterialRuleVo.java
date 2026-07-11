package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AutoMapper(target = ProductPriceMaterialRule.class)
public class ProductPriceMaterialRuleVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
