package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品基础属性表 pc_base_attribute
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_base_attribute")
@Schema(description = "产品基础属性表")
public class ProductBaseAttribute extends BaseEntity {

    @TableId(value = "attribute_id")
    @Schema(description = "属性ID")
    private Long attributeId;

    @Schema(description = "属性组")
    private String attributeGroup;

    @Schema(description = "属性编码")
    private String attributeCode;

    @Schema(description = "属性中文名称")
    private String attributeNameCn;

    @Schema(description = "属性英文名称")
    private String attributeNameEn;

    @Schema(description = "值类型")
    private String valueType;

    @Schema(description = "默认单位编码")
    private String unitCode;

    @Schema(description = "适用物料类型")
    private String materialTypes;

    @Schema(description = "扩展配置JSON")
    private String extraJson;

    @Schema(description = "状态：1正常，0停用")
    private String status;

    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
