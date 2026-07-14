package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ProductionQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ProductionOrderVo;
import com.bocoo.dealer.fulfillment.service.FactoryProductionService;
import com.bocoo.dealer.fulfillment.service.ProductionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FactoryProductionServiceImpl implements FactoryProductionService {
    private final ProductionService delegate;

    public TableDataInfo<ProductionOrderVo> queryPage(ProductionQueryBo bo, PageQuery pageQuery) {
        return delegate.queryPage(bo, pageQuery);
    }
    public FulfillmentOrderVo detail(Long id) { return delegate.detail(id, FulfillmentAudience.FACTORY); }
    public Boolean start(Long id) { return delegate.start(id); }
    public Boolean complete(Long id, String note) { return delegate.complete(id, note); }
}
