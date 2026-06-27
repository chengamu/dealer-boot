package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductFormulaUsageRule.class, reverseConvertGenerate = false)
public class ProductFormulaUsageRuleBo extends BaseBo {
    private Long usageRuleId;
    private Long tenantId;
    private Long formulaId;
    private Long formulaMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String ruleName;
    private String conditionType;
    private String conditionOptionCode;
    private String conditionOptionNameCn;
    private String conditionValueCode;
    private String conditionValueNameCn;
    private String conditionExpression;
    private String conditionText;
    private String conditionKey;
    private String usageMode;
    private BigDecimal fixedUsageQty;
    private String usageFormula;
    private String usageFormulaText;
    private String calculationUnitCode;
    private String roundingMode;
    private BigDecimal minUsageQty;
    private BigDecimal maxUsageQty;
    private BigDecimal lossRate;
    private Boolean defaultRuleFlag;
    private String productionRemark;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
