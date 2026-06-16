package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_engineering_item_scope")
@Schema(description = "工程构成项可选范围")
public class EngineeringItemScope extends BaseEntity {
    @TableId(value = "scope_id")
    private Long scopeId;
    private Long tenantId;
    private Long versionId;
    private Long itemId;
    private String itemCode;
    private String scopeType;
    private String scopeCode;
    private String scopeNameCn;
    private String scopeNameEn;
    private String includeFlag;
    private String conditionJson;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
