package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 产品单位表 pc_unit
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_unit")
@Schema(description = "产品单位表")
public class ProductUnit extends BaseEntity {

    @TableId(value = "unit_id")
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
}
