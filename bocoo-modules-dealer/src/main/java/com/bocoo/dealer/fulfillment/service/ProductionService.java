package com.bocoo.dealer.fulfillment.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.fulfillment.domain.bo.ProductionQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ProductionOrderVo;
import com.bocoo.dealer.fulfillment.service.impl.FulfillmentAudience;

public interface ProductionService {
    TableDataInfo<ProductionOrderVo> queryPage(ProductionQueryBo bo, PageQuery pageQuery);

    FulfillmentOrderVo detail(Long salesDocumentId, FulfillmentAudience audience);

    Boolean start(Long salesDocumentId);

    Boolean complete(Long salesDocumentId, String note);
}
