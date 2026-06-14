package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.SalesProduct;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售产品视图对象
 */
@Data
@AutoMapper(target = SalesProduct.class)
@Schema(description = "销售产品视图对象")
public class SalesProductVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "销售产品ID")
    private Long salesProductId;

    @Schema(description = "销售产品编码")
    private String salesProductCode;

    @Schema(description = "销售产品中文名称")
    private String salesProductNameCn;

    @Schema(description = "销售产品英文名称")
    private String salesProductNameEn;

    @Schema(description = "产品分类ID")
    private Long categoryId;

    @Schema(description = "产品分类编码")
    private String categoryCode;

    @Schema(description = "产品分类中文名称")
    private String categoryNameCn;

    @Schema(description = "产品分类英文名称")
    private String categoryNameEn;

    @Schema(description = "产品类型")
    private String productType;

    @Schema(description = "销售模式")
    private String salesMode;

    @Schema(description = "配置模板ID")
    private Long templateId;

    @Schema(description = "配置模板编码")
    private String templateCode;

    @Schema(description = "当前模板版本ID")
    private Long templateVersionId;

    @Schema(description = "当前模板版本号")
    private String templateVersionNo;

    @Schema(description = "默认宽度")
    private BigDecimal defaultWidth;

    @Schema(description = "默认高度")
    private BigDecimal defaultHeight;

    @Schema(description = "尺寸单位")
    private String dimensionUnit;

    @Schema(description = "业务状态")
    private String bizStatus;

    @Schema(description = "旧系统来源")
    private String legacySource;

    @Schema(description = "旧系统编号")
    private String legacyId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "删除标志")
    private String delFlag;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间，UTC语义")
    private LocalDateTime createTime;

    @Schema(description = "更新时间，UTC语义")
    private LocalDateTime updateTime;
}
