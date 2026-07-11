package com.bocoo.dealer.domain.vo;

import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Data
@AutoMapper(target = SalesDocumentItem.class)
public class SalesDocumentItemVo implements Serializable {
    private Long salesItemId;
    private Long salesDocumentId;
    private Long sourceQuoteItemId;
    private Integer lineNo;
    private String itemCode;
    private String roomLocation;
    private Long saleProductId;
    private String saleProductCode;
    private String saleProductName;
    private String categoryNameCn;
    private String productTypeNameCn;
    private Long formulaId;
    private Long formulaVersionId;
    private String formulaVersionLabel;
    private BigDecimal orderWidthInch;
    private BigDecimal orderHeightInch;
    private Integer quantity;
    private Map<String, String> selectedOptionValues;
    private String configurationSummary;
    private String calculationStatus;
    private String calculationMessage;
    private BigDecimal listUnitAmount;
    private BigDecimal listAmount;
    private BigDecimal discountRate;
    private BigDecimal unitAmount;
    private BigDecimal productAmount;
    private BigDecimal shippingAmount;
    private BigDecimal lineAmount;
    private Integer sortOrder;
    private String remark;
}
