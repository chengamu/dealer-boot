package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品业务字典项 pc_product_dict_item
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_product_dict_item")
@Schema(description = "产品业务字典项")
public class ProductDictItem extends BaseEntity {

    @TableId(value = "dict_item_id")
    @Schema(description = "字典项ID")
    private Long dictItemId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "字典类型编码")
    private String dictTypeCode;

    @Schema(description = "字典项值")
    private String dictItemValue;

    @Schema(description = "字典项中文标签")
    private String dictItemLabelCn;

    @Schema(description = "字典项英文标签")
    private String dictItemLabelEn;

    @Schema(description = "父级值")
    private String parentValue;

    @Schema(description = "系统内置")
    private Boolean systemFlag;

    @Schema(description = "允许维护")
    private Boolean editableFlag;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "删除标志")
    private String delFlag;
}
