package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductMaterialAttribute;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 产品物料属性值业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductMaterialAttribute.class, reverseConvertGenerate = false)
@Schema(description = "产品物料属性值业务对象")
public class ProductMaterialAttributeBo extends BaseBo {

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
