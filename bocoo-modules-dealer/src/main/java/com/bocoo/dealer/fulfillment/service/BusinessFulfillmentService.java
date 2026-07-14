package com.bocoo.dealer.fulfillment.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.*;

import java.util.List;

public interface BusinessFulfillmentService {
    TableDataInfo<ShipmentOrderVo> queryPage(ShipmentQueryBo bo, PageQuery pageQuery);
    FulfillmentOrderVo detail(Long salesDocumentId);
    List<ShipmentVo> shipments(Long salesDocumentId);
    List<TrackingEventVo> trackingEvents(Long shipmentId);
    List<TrackingSummaryVo> trackingSummaries(List<Long> shipmentIds);
    Boolean confirmReceipt(Long shipmentId);
}
