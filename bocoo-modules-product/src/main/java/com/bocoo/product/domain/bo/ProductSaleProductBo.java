package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 可售产品业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductSaleProduct.class, reverseConvertGenerate = false)
@Schema(description = "可售产品业务对象")
public class ProductSaleProductBo extends BaseBo {
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
