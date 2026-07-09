package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductFormulaUsageRule.class)
public class ProductFormulaUsageRuleVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long usageRuleId;
    private Long tenantId;
    private Long formulaId;
    private Long formulaMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String ruleName;
    private String conditionType;
    private String conditionOptionRefKey;
    private String conditionOptionCode;
    private String conditionOptionNameCn;
    private String conditionValueRefKey;
    private String conditionValueCode;
    private String conditionValueNameCn;
    private String conditionJson;
    private String conditionExpression;
    private String conditionText;
    private String conditionKey;
    private String usageMode;
    private BigDecimal fixedUsageQty;
    private String lengthFormula;
    private String lengthFormulaText;
    private String widthFormula;
    private String widthFormulaText;
    private String heightFormula;
    private String heightFormulaText;
    private String weightFormula;
    private String weightFormulaText;
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
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
