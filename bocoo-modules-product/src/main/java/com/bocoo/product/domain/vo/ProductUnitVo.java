package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductUnit;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 产品单位视图对象
 */
@Data
@AutoMapper(target = ProductUnit.class)
@Schema(description = "产品单位视图对象")
public class ProductUnitVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "单位ID")
    private Long unitId;

    @Schema(description = "单位编码")
    private String unitCode;

    @Schema(description = "单位中文名称")
    private String unitNameCn;

    @Schema(description = "单位英文名称")
    private String unitNameEn;

    @Schema(description = "单位类型")
    private String unitType;

    @Schema(description = "小数精度")
    private Integer precisionScale;

    @Schema(description = "舍入方式")
    private String roundingMode;

    @Schema(description = "基准单位编码")
    private String baseUnitCode;

    @Schema(description = "换算系数")
    private BigDecimal conversionRate;

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
