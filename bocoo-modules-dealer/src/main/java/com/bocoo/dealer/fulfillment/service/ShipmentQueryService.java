package com.bocoo.dealer.fulfillment.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;
import com.bocoo.dealer.fulfillment.service.impl.FulfillmentAudience;

import java.util.List;

public interface ShipmentQueryService {
    TableDataInfo<ShipmentOrderVo> queryPage(ShipmentQueryBo bo, PageQuery pageQuery, FulfillmentAudience audience);

    List<ShipmentVo> orderShipments(Long salesDocumentId, FulfillmentAudience audience);

    ShipmentVo detail(Long shipmentId, FulfillmentAudience audience);
}
