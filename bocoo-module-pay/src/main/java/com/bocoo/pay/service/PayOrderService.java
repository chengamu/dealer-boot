package com.bocoo.pay.service;

import com.bocoo.pay.domain.bo.PayOrderCreateBo;
import com.bocoo.pay.domain.bo.PaySubmitBo;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.vo.PaySubmitVo;

/**
 * Payment order service contract.
 */
public interface PayOrderService {

    PayOrder createOrder(PayOrderCreateBo bo);

    PaySubmitVo submitOrder(PaySubmitBo bo);

    PayOrder getOrderByNo(String no);

    boolean notifyOrderSuccess(String channelCode, String channelOrderNo, String rawNotifyData);
}
