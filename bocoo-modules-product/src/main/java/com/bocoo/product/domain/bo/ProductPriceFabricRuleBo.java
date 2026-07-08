package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductPriceFabricRule.class, reverseConvertGenerate = false)
@Schema(description = "面料价格规则业务对象")
public class ProductPriceFabricRuleBo extends BaseBo {
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
