package com.bocoo.dealer.fulfillment.service;

import com.bocoo.dealer.fulfillment.domain.bo.ShipmentBo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;

public interface ShipmentCommandService {
    ShipmentVo create(Long salesDocumentId, ShipmentBo bo);

    Boolean update(Long shipmentId, ShipmentBo bo);

    Boolean delete(Long shipmentId);

    Boolean dispatch(Long shipmentId);
}
