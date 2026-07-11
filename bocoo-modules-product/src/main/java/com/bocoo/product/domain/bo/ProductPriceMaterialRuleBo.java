package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductPriceMaterialRule.class, reverseConvertGenerate = false)
@Schema(description = "物料价格规则业务对象")
public class ProductPriceMaterialRuleBo extends BaseBo {
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
