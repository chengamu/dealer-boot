package com.bocoo.pay.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.ReconciliationCaseQueryBo;
import com.bocoo.pay.domain.vo.ReconciliationCaseVo;
import com.bocoo.pay.domain.vo.ReconciliationCaseDetailVo;

public interface PaymentReconciliationService {
    TableDataInfo<ReconciliationCaseVo> list(ReconciliationCaseQueryBo query, PageQuery pageQuery);

    ReconciliationCaseDetailVo detail(Long caseId);

    ReconciliationCaseVo scanPayment(Long payOrderId);

    ReconciliationCaseVo rescan(Long caseId, String reason);

    ReconciliationCaseVo reconcileChannel(Long caseId, String reason);

    ReconciliationCaseVo repairOrder(Long caseId, String reason);

    ReconciliationCaseVo ignore(Long caseId, String reason);
}
