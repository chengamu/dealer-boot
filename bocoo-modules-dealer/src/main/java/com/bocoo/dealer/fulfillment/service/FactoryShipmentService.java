package com.bocoo.dealer.fulfillment.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentBo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;

public interface FactoryShipmentService {
    TableDataInfo<ShipmentOrderVo> queryPage(ShipmentQueryBo bo, PageQuery pageQuery);
    FulfillmentOrderVo detail(Long salesDocumentId);
    ShipmentVo create(Long salesDocumentId, ShipmentBo bo);
    Boolean update(Long shipmentId, ShipmentBo bo);
    Boolean delete(Long shipmentId);
    Boolean dispatch(Long shipmentId);
}
