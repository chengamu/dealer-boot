package com.bocoo.pay.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;
import com.bocoo.pay.domain.vo.PayOrderDetailVo;
import com.bocoo.pay.domain.vo.PaymentOrderSummaryVo;

public interface BusinessPaymentQueryService {
    TableDataInfo<PaymentOrderSummaryVo> list(PaymentOrderQueryBo query, PageQuery pageQuery);

    PayOrderDetailVo detail(Long payOrderId);
}
