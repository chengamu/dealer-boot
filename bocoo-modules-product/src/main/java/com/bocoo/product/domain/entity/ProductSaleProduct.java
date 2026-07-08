package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 可售产品 pc_sale_product
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_sale_product")
@Schema(description = "可售产品")
public class ProductSaleProduct extends BaseEntity {

    @TableId(value = "sale_product_id")
    private Long saleProductId;
    private Long tenantId;
    private String saleProductCode;
    private String saleProductName;
    private Long categoryId;
    private String categoryCode;
    private String categoryNameCn;
    private String productTypeCode;
    private String productTypeNameCn;
    private Long formulaId;
    private String formulaCode;
    private String formulaName;
    private Long formulaVersionId;
    private Integer formulaVersionNo;
    private String formulaVersionLabel;
    private BigDecimal minWidthInch;
    private BigDecimal minHeightInch;
    private BigDecimal maxWidthInch;
    private BigDecimal maxHeightInch;
    private String sizeSummary;
    private String priceStatus;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
