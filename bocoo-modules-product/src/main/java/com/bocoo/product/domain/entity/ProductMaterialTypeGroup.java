package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_material_type_group")
@Schema(description = "物料类型分组表")
public class ProductMaterialTypeGroup extends BaseEntity {

    @TableId(value = "group_id")
    @Schema(description = "分组ID")
    private Long groupId;

    @Schema(description = "分组编码")
    private String groupCode;

    @Schema(description = "分组中文名称")
    private String groupNameCn;

    @Schema(description = "分组英文名称")
    private String groupNameEn;

    @Schema(description = "系统内置")
    private Boolean systemFlag;

    @Schema(description = "允许维护")
    private Boolean editableFlag;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "删除标志")
    private String delFlag;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
