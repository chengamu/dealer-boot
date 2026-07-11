package com.bocoo.merchant.domain.vo;

import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@AutoMapper(target = CustomerQuoteItem.class)
public class CustomerQuoteItemVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private Long quoteItemId;
    private Long quoteId;
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
    private Map<String, String> selectedOptionValues = new LinkedHashMap<>();
    private String selectedOptionsSummaryCn;
    private String selectedOptionsSummaryEn;
    private String calculationStatus;
    private String calculationMessage;
    private BigDecimal unitAmount;
    private BigDecimal productAmount;
    private BigDecimal unitShippingAmount;
    private Long shippingTemplateId;
    private BigDecimal shippingAmount;
    private BigDecimal lineAmount;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime updateTime;
}
