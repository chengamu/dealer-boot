package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 产品物料属性值表 pc_material_attribute
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_material_attribute")
@Schema(description = "产品物料属性值表")
public class ProductMaterialAttribute extends BaseEntity {

    @TableId(value = "material_attribute_id")
    @Schema(description = "物料属性值ID")
    private Long attributeValueId;

    @Schema(description = "物料ID")
    private Long materialId;

    @Schema(description = "物料编码快照")
    private String materialCode;

    @Schema(description = "属性ID")
    private Long attributeId;

    @Schema(description = "属性编码快照")
    private String attributeCode;

    @Schema(description = "属性中文名称快照")
    private String attributeNameCn;

    @Schema(description = "文本值")
    private String valueText;

    @Schema(description = "数值")
    private BigDecimal valueNumber;

    @Schema(description = "布尔值")
    private Boolean valueBool;

    @Schema(description = "数值单位")
    private String valueUnitCode;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：1正常，0停用")
    private String status;

    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    @Schema(description = "备注")
    private String remark;
}
