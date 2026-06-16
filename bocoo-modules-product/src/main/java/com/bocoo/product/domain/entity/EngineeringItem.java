package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_engineering_item")
@Schema(description = "工程构成项")
public class EngineeringItem extends BaseEntity {
    @TableId(value = "item_id")
    private Long itemId;
    private Long tenantId;
    private Long versionId;
    private Long planId;
    private String itemCode;
    private String itemNameCn;
    private String itemNameEn;
    private String itemType;
    private String sourceType;
    private String requiredFlag;
    private String multiSelectFlag;
    private String customerSelectable;
    private String defaultSourceCode;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String extraJson;
    private String remark;
}
