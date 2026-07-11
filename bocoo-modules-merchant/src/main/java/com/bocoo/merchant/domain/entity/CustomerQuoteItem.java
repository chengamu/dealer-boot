package com.bocoo.merchant.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("customer_quote_item")
public class CustomerQuoteItem extends BaseEntity {

    @TableId(value = "quote_item_id")
    private Long quoteItemId;
    private Long quoteId;
    private Long tenantId;
    private Integer lineNo;
    private String roomLocation;
    private Long saleProductId;
    private String saleProductCode;
    private String saleProductName;
    private Long formulaId;
    private Long formulaVersionId;
    private String formulaVersionLabel;
    private BigDecimal orderWidthInch;
    private BigDecimal orderHeightInch;
    private Integer quantity;
    private String selectedOptionsJson;
    private String selectedOptionsSummaryCn;
    private String selectedOptionsSummaryEn;
    private String calculationStatus;
    private String calculationMessage;
    private BigDecimal unitAmount;
    private BigDecimal productAmount;
    private BigDecimal unitShippingAmount;
    private Long shippingTemplateId;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private BigDecimal lineAmount;
    private String pricingSnapshotJson;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
