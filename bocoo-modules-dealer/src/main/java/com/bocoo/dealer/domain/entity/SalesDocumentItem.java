package com.bocoo.dealer.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dealer_sales_document_item")
public class SalesDocumentItem extends BaseEntity {
    @TableId(value = "sales_item_id")
    private Long salesItemId;
    private Long salesDocumentId;
    private Long sourceQuoteItemId;
    private Long sourceQuickOrderItemId;
    private Long tenantId;
    private Integer lineNo;
    private String itemCode;
    private String roomLocation;
    private Long saleProductId;
    private String saleProductCode;
    private String saleProductName;
    private Long categoryId;
    private String categoryCode;
    private String categoryNameCn;
    private String productTypeCode;
    private String productTypeNameCn;
    private Long formulaId;
    private Long formulaVersionId;
    private String formulaVersionLabel;
    private BigDecimal orderWidthInch;
    private BigDecimal orderHeightInch;
    private Integer quantity;
    private String selectedOptionsJson;
    private String configurationSummary;
    private String configurationSummaryCn;
    private String configurationSummaryEn;
    private String calculationStatus;
    private String calculationMessage;
    private BigDecimal listUnitAmount;
    private BigDecimal listAmount;
    private BigDecimal discountRate;
    private BigDecimal discountAmount;
    private BigDecimal unitAmount;
    private BigDecimal productAmount;
    private Long shippingTemplateId;
    private BigDecimal unitShippingAmount;
    private BigDecimal shippingAmount;
    private BigDecimal lineAmount;
    private String bomSnapshotJson;
    private String pricingSnapshotJson;
    private String shippingSnapshotJson;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
