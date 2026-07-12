package com.bocoo.dealer.fulfillment.domain.vo;

import com.bocoo.dealer.fulfillment.domain.entity.ShipmentItem;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;

@Data
@AutoMapper(target = ShipmentItem.class)
public class ShipmentItemVo implements Serializable {
    private Long shipmentItemId;
    private Long shipmentId;
    private Long salesDocumentId;
    private Long salesItemId;
    private Integer lineNo;
    private String saleProductCode;
    private String saleProductName;
    private Integer quantity;
    private String remark;
}
