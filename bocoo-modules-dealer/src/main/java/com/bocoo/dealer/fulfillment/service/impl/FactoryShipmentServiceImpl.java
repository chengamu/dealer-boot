package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentBo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;
import com.bocoo.dealer.fulfillment.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FactoryShipmentServiceImpl implements FactoryShipmentService {
    private final ShipmentQueryService queryService;
    private final ProductionService productionService;
    private final ShipmentCommandService commandService;

    public TableDataInfo<ShipmentOrderVo> queryPage(ShipmentQueryBo bo, PageQuery pageQuery) {
        return queryService.queryPage(bo, pageQuery, FulfillmentAudience.FACTORY);
    }
    public FulfillmentOrderVo detail(Long id) { return productionService.detail(id, FulfillmentAudience.FACTORY); }
    public ShipmentVo create(Long id, ShipmentBo bo) { return commandService.create(id, bo); }
    public Boolean update(Long id, ShipmentBo bo) { return commandService.update(id, bo); }
    public Boolean delete(Long id) { return commandService.delete(id); }
    public Boolean dispatch(Long id) { return commandService.dispatch(id); }
}
