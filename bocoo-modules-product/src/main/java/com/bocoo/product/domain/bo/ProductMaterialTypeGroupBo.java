package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductMaterialTypeGroup.class, reverseConvertGenerate = false)
@Schema(description = "物料类型分组业务对象")
public class ProductMaterialTypeGroupBo extends BaseBo {

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
