package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 销售产品表 pc_sales_product
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_sales_product")
@Schema(description = "销售产品表")
public class SalesProduct extends BaseEntity {

    @TableId(value = "sales_product_id")
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
    private java.math.BigDecimal defaultWidth;

    @Schema(description = "默认高度")
    private java.math.BigDecimal defaultHeight;

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
}
