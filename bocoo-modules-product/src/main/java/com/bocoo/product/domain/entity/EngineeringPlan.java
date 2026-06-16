package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_engineering_plan")
@Schema(description = "工程方案")
public class EngineeringPlan extends BaseEntity {
    @TableId(value = "plan_id")
    private Long planId;
    private Long tenantId;
    private String planCode;
    private String planNameCn;
    private String planNameEn;
    private Long categoryId;
    private String categoryCode;
    private String categoryNameCn;
    private String categoryNameEn;
    private Long seriesId;
    private String seriesCode;
    private String seriesNameCn;
    private String seriesNameEn;
    private Long currentVersionId;
    private String currentVersionNo;
    private String bizStatus;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
