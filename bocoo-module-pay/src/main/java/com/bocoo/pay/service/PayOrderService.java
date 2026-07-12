package com.bocoo.pay.service;

import com.bocoo.pay.domain.bo.PayOrderCreateBo;
import com.bocoo.pay.domain.bo.PaySubmitBo;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.vo.PaySubmitVo;

import java.util.List;

/**
 * Payment order service contract.
 */
public interface PayOrderService {

    PayOrder createOrder(PayOrderCreateBo bo);

    PaySubmitVo submitOrder(PaySubmitBo bo);

    PayOrder getOrderByNo(String no);

    PayOrder getOrderByMerchantOrderId(Long payerTenantId, String merchantOrderId);

    List<PayOrderExtension> getAttempts(Long payOrderId);

    boolean notifyOrderSuccess(String channelCode, String channelOrderNo, String rawNotifyData);
}
