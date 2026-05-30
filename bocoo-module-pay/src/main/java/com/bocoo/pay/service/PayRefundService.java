package com.bocoo.pay.service;

import com.bocoo.pay.domain.bo.PayRefundCreateBo;
import com.bocoo.pay.domain.entity.PayRefund;

/**
 * Payment refund service contract.
 */
public interface PayRefundService {

    PayRefund createRefund(PayRefundCreateBo bo);

    boolean notifyRefundSuccess(String channelCode, String channelRefundNo, String rawNotifyData);
}
