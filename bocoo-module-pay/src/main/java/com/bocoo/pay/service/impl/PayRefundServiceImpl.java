package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.pay.domain.bo.PayRefundCreateBo;
import com.bocoo.pay.domain.entity.PayRefund;
import com.bocoo.pay.mapper.PayRefundMapper;
import com.bocoo.pay.service.PayRefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Payment refund service implementation.
 */
@RequiredArgsConstructor
@Service
public class PayRefundServiceImpl implements PayRefundService {

    private final PayRefundMapper payRefundMapper;

    @Override
    public PayRefund createRefund(PayRefundCreateBo bo) {
        throw new ServiceException("Refund implementation is pending channel migration");
    }

    @Override
    public boolean notifyRefundSuccess(String channelCode, String channelRefundNo, String rawNotifyData) {
        PayRefund refund = payRefundMapper.selectOne(new LambdaQueryWrapper<PayRefund>()
            .eq(PayRefund::getChannelCode, channelCode)
            .eq(PayRefund::getChannelRefundNo, channelRefundNo), false);
        return refund != null;
    }
}
