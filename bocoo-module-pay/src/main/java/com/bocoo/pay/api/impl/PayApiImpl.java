package com.bocoo.pay.api.impl;

import com.bocoo.pay.api.PayApi;
import com.bocoo.pay.domain.bo.PayOrderCreateBo;
import com.bocoo.pay.domain.bo.PaySubmitBo;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.vo.PayOrderStatusVo;
import com.bocoo.pay.domain.vo.PaySubmitVo;
import com.bocoo.pay.service.PayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Internal payment API implementation.
 */
@RequiredArgsConstructor
@Service
public class PayApiImpl implements PayApi {

    private final PayOrderService payOrderService;

    @Override
    public Long createOrder(PayOrderCreateBo bo) {
        return payOrderService.createOrder(bo).getId();
    }

    @Override
    public PaySubmitVo submitOrder(PaySubmitBo bo) {
        return payOrderService.submitOrder(bo);
    }

    @Override
    public PayOrderStatusVo getOrderStatus(String orderNo) {
        PayOrder order = payOrderService.getOrderByNo(orderNo);
        if (order == null) {
            return null;
        }
        PayOrderStatusVo vo = new PayOrderStatusVo();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getNo());
        vo.setMerchantOrderId(order.getMerchantOrderId());
        vo.setPayerTenantId(order.getPayerTenantId());
        vo.setPayeeTenantId(order.getPayeeTenantId());
        vo.setChannelCode(order.getChannelCode());
        vo.setPrice(order.getPrice());
        vo.setCurrency(order.getCurrency());
        vo.setStatus(order.getStatus());
        vo.setSuccessTime(order.getSuccessTime());
        return vo;
    }

    @Override
    public boolean notifyOrderSuccess(String channelCode, String channelOrderNo, String rawNotifyData) {
        return payOrderService.notifyOrderSuccess(channelCode, channelOrderNo, rawNotifyData);
    }
}
