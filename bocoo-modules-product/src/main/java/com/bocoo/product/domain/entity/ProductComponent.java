package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 产品组件表 pc_component
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_component")
@Schema(description = "产品组件表")
public class ProductComponent extends BaseEntity {

    /**
     * 产品组件ID
     */
    @TableId(value = "component_id")
    @Schema(description = "产品组件ID")
    private Long componentId;

    /**
     * 组件编码
     */
    @Schema(description = "组件编码")
    private String componentCode;

    /**
     * 组件中文名称
     */
    @Schema(description = "组件中文名称")
    private String componentNameCn;

    /**
     * 组件英文名称
     */
    @Schema(description = "组件英文名称")
    private String componentNameEn;

    /**
     * 组件类型
     */
    @Schema(description = "组件类型")
    private String componentType;

    /**
     * 关联物料ID
     */
    @Schema(description = "关联物料ID")
    private Long materialId;

    /**
     * 关联物料编码快照
     */
    @Schema(description = "关联物料编码快照")
    private String materialCode;

    /**
     * 关联物料中文名称快照
     */
    @Schema(description = "关联物料中文名称快照")
    private String materialNameCn;

    /**
     * 关联物料英文名称快照
     */
    @Schema(description = "关联物料英文名称快照")
    private String materialNameEn;

    /**
     * 默认数量
     */
    @Schema(description = "默认数量")
    private BigDecimal defaultQty;

    /**
     * 数量模式
     */
    @Schema(description = "数量模式")
    private String qtyMode;

    /**
     * 单位编码
     */
    @Schema(description = "单位编码")
    private String unitCode;

    /**
     * 状态：1正常，0停用
     */
    @Schema(description = "状态：1正常，0停用")
    private String status;

    /**
     * 删除标志：0存在，2删除
     */
    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    /**
     * 适用范围JSON
     */
    @Schema(description = "适用范围JSON")
    private String scopeJson;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
