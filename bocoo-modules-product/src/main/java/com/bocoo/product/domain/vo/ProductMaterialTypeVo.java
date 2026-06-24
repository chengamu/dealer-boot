package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductMaterialType;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductMaterialType.class)
@Schema(description = "物料类型视图对象")
public class ProductMaterialTypeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "物料类型ID")
    private Long materialTypeId;

    @Schema(description = "物料类型编码")
    private String materialTypeCode;

    @Schema(description = "物料类型中文名称")
    private String materialTypeNameCn;

    @Schema(description = "物料类型英文名称")
    private String materialTypeNameEn;

    @Schema(description = "属性分组ID")
    private Long attributeGroupId;

    @Schema(description = "属性分组编码")
    private String attributeGroupCode;

    @Schema(description = "属性分组中文名称")
    private String attributeGroupNameCn;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
