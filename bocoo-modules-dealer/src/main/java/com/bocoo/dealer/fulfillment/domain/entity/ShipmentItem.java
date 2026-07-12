package com.bocoo.dealer.fulfillment.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dealer_shipment_item")
public class ShipmentItem extends BaseEntity {
    @TableId(value = "shipment_item_id")
    private Long shipmentItemId;
    private Long tenantId;
    private Long shipmentId;
    private Long salesDocumentId;
    private Long salesItemId;
    private Integer lineNo;
    private String saleProductCode;
    private String saleProductName;
    private Integer quantity;
    private String remark;
}
