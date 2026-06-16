package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品业务字典类型 pc_product_dict_type
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_product_dict_type")
@Schema(description = "产品业务字典类型")
public class ProductDictType extends BaseEntity {

    @TableId(value = "dict_type_id")
    @Schema(description = "字典类型ID")
    private Long dictTypeId;

    @Schema(description = "租户ID")
    private Long tenantId;

    @Schema(description = "字典类型编码")
    private String dictTypeCode;

    @Schema(description = "字典类型中文名称")
    private String dictTypeNameCn;

    @Schema(description = "字典类型英文名称")
    private String dictTypeNameEn;

    @Schema(description = "业务域")
    private String businessDomain;

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
