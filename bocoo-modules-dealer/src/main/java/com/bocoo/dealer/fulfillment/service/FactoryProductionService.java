package com.bocoo.dealer.fulfillment.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ProductionQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ProductionOrderVo;

public interface FactoryProductionService {
    TableDataInfo<ProductionOrderVo> queryPage(ProductionQueryBo bo, PageQuery pageQuery);
    FulfillmentOrderVo detail(Long salesDocumentId);
    Boolean start(Long salesDocumentId);
    Boolean complete(Long salesDocumentId, String note);
}
