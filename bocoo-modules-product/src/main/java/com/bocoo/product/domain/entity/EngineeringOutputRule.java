package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_engineering_output_rule")
@Schema(description = "工程带出规则")
public class EngineeringOutputRule extends BaseEntity {
    @TableId(value = "output_rule_id")
    private Long outputRuleId;
    private Long tenantId;
    private Long versionId;
    private String ruleCode;
    private String ruleNameCn;
    private String ruleNameEn;
    private String conditionJson;
    private String outputType;
    private String outputCode;
    private String outputNameCn;
    private String outputNameEn;
    private BigDecimal defaultQty;
    private String unitCode;
    private String reasonCn;
    private String reasonEn;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
