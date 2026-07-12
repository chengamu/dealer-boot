package com.bocoo.dealer.fulfillment.domain.vo;

import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AutoMapper(target = SalesDocumentItem.class)
public class FulfillmentItemVo {
    private Long salesItemId;
    private Integer lineNo;
    private String roomLocation;
    private String saleProductCode;
    private String saleProductName;
    private String formulaVersionLabel;
    private BigDecimal orderWidthInch;
    private BigDecimal orderHeightInch;
    private Integer quantity;
    private String configurationSummary;
    private String bomSnapshotJson;
}
