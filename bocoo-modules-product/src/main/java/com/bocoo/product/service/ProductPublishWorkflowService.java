package com.bocoo.product.service;

import com.bocoo.product.domain.bo.PublishCheckBo;
import com.bocoo.product.domain.vo.PublishCheckSummaryVo;
import com.bocoo.product.domain.vo.PublishExecutionResultVo;

public interface ProductPublishWorkflowService {

    PublishCheckSummaryVo check(PublishCheckBo bo);

    PublishExecutionResultVo publish(PublishCheckBo bo);
}
