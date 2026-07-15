package com.bocoo.dealer.operations.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.operations.domain.bo.OperationsMerchantQueryBo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantLevelOptionVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantVo;
import com.bocoo.dealer.operations.domain.vo.OperationsSummaryVo;

import java.util.List;

public interface PlatformOperationsOverviewService {

    OperationsSummaryVo summary();

    TableDataInfo<OperationsMerchantVo> merchantPage(OperationsMerchantQueryBo query, PageQuery pageQuery);

    List<OperationsMerchantLevelOptionVo> merchantLevelOptions(String status);

    TableDataInfo<OperationsMerchantVo> pendingMerchantApplications(OperationsMerchantQueryBo query, PageQuery pageQuery);
}
