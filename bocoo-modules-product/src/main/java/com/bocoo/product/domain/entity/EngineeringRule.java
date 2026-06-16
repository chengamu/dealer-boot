package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_engineering_rule")
@Schema(description = "工程能力规则")
public class EngineeringRule extends BaseEntity {
    @TableId(value = "rule_id")
    private Long ruleId;
    private Long tenantId;
    private Long versionId;
    private String ruleCode;
    private String ruleNameCn;
    private String ruleNameEn;
    private String ruleType;
    private String conditionJson;
    private String actionJson;
    private String severity;
    private String messageCn;
    private String messageEn;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
