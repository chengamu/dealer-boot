package com.bocoo.pay.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.PaySupplementBo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.domain.vo.PayOrderDetailVo;
import com.bocoo.pay.domain.vo.PaymentOrderSummaryVo;

public interface PlatformPaymentOrderService {
    TableDataInfo<PaymentOrderSummaryVo> list(PaymentOrderQueryBo query, PageQuery pageQuery);

    PayOrderDetailVo detail(Long payOrderId);

    PayAttemptVo supplement(Long payOrderId, PaySupplementBo bo);
}
