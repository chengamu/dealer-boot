package com.bocoo.dealer.quickorder.domain.vo;

import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AutoMapper(target = QuickOrderItem.class)
public class QuickOrderItemVo {
    private Long quickOrderItemId;
    private Long quickOrderId;
    private Integer lineNo;
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
    private Map<String, String> selectedOptionValues = new LinkedHashMap<>();
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
    private Integer sortOrder;
    private String remark;
}
