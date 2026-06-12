package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductBaseAttribute;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品基础属性视图对象
 */
@Data
@AutoMapper(target = ProductBaseAttribute.class)
@Schema(description = "产品基础属性视图对象")
public class ProductBaseAttributeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
