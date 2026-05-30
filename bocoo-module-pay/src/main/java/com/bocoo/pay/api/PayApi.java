package com.bocoo.pay.api;

import com.bocoo.pay.domain.bo.PayOrderCreateBo;
import com.bocoo.pay.domain.bo.PaySubmitBo;
import com.bocoo.pay.domain.vo.PayOrderStatusVo;
import com.bocoo.pay.domain.vo.PaySubmitVo;

/**
 * Internal payment API for business order modules.
 */
public interface PayApi {

    Long createOrder(PayOrderCreateBo bo);

    PaySubmitVo submitOrder(PaySubmitBo bo);

    PayOrderStatusVo getOrderStatus(String orderNo);

    boolean notifyOrderSuccess(String channelCode, String channelOrderNo, String rawNotifyData);
}
