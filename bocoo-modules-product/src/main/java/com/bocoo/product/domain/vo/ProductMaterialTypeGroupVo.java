package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductMaterialTypeGroup.class)
@Schema(description = "物料类型分组视图对象")
public class ProductMaterialTypeGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Schema(description = "配方统计展示")
    private Boolean formulaSummaryVisibleFlag;

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
