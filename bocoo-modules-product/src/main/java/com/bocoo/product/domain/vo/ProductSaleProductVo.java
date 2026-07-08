package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductSaleProduct;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductSaleProduct.class)
@Schema(description = "可售产品视图对象")
public class ProductSaleProductVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
