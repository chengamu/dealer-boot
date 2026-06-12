package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.FabricSeries;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 面料系列业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = FabricSeries.class, reverseConvertGenerate = false)
@Schema(description = "面料系列业务对象")
public class FabricSeriesBo extends BaseBo {

    @Schema(description = "系列ID")
    private Long seriesId;

    @Schema(description = "系列编码")
    private String seriesCode;

    @Schema(description = "系列中文名称")
    private String seriesNameCn;

    @Schema(description = "系列英文名称")
    private String seriesNameEn;

    @Schema(description = "物料类型")
    private String materialType;

    @Schema(description = "默认厚度单位")
    private String defaultThicknessUnit;

    @Schema(description = "默认厚度值")
    private BigDecimal defaultThicknessValue;

    @Schema(description = "厚度规则开关")
    private Boolean thicknessRuleEnabled;

    @Schema(description = "最大厚度差")
    private BigDecimal maxThicknessDiff;

    @Schema(description = "最大组合厚度")
    private BigDecimal maxCombinedThickness;

    @Schema(description = "门幅规则开关")
    private Boolean widthRuleEnabled;

    @Schema(description = "可用门幅列表")
    private String availableWidths;

    @Schema(description = "最小门幅")
    private BigDecimal minWidthValue;

    @Schema(description = "最大门幅")
    private BigDecimal maxWidthValue;

    @Schema(description = "门幅单位")
    private String widthUnit;

    @Schema(description = "扩展规则JSON")
    private String extraRuleJson;

    @Schema(description = "状态：1正常，0停用")
    private String status;

    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    @Schema(description = "备注")
    private String remark;
}
