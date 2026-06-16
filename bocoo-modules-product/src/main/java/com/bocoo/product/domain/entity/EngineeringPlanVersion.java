package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_engineering_plan_version")
@Schema(description = "工程方案版本")
public class EngineeringPlanVersion extends BaseEntity {
    @TableId(value = "version_id")
    private Long versionId;
    private Long tenantId;
    private Long planId;
    private String planCode;
    private String versionNo;
    private String versionName;
    private String bizStatus;
    private String status;
    private String delFlag;
    private String ruleSchemaVersion;
    private String configJson;
    private String remark;
}
