package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 产品组件明细表 pc_component_item
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_component_item")
@Schema(description = "产品组件明细表")
public class ProductComponentItem extends BaseEntity {

    @TableId(value = "component_item_id")
    @Schema(description = "组件明细ID")
    private Long componentItemId;

    @Schema(description = "组件ID")
    private Long componentId;

    @Schema(description = "组件编码快照")
    private String componentCode;

    @Schema(description = "物料ID")
    private Long materialId;

    @Schema(description = "物料编码快照")
    private String materialCode;

    @Schema(description = "物料中文名称快照")
    private String materialNameCn;

    @Schema(description = "数量公式")
    private String qtyFormula;

    @Schema(description = "默认数量")
    private BigDecimal defaultQty;

    @Schema(description = "单位编码")
    private String unitCode;

    @Schema(description = "是否必选")
    private Boolean requiredFlag;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：1正常，0停用")
    private String status;

    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    @Schema(description = "备注")
    private String remark;
}
