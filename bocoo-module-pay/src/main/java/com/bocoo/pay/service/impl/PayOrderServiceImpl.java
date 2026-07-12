package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.bo.PayOrderCreateBo;
import com.bocoo.pay.domain.bo.PaySubmitBo;
import com.bocoo.pay.domain.bo.PayWalletChangeBo;
import com.bocoo.pay.domain.entity.PayApp;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.entity.PayWallet;
import com.bocoo.pay.domain.vo.PaySubmitVo;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.enums.PayOrderStatus;
import com.bocoo.pay.enums.PayWalletBizType;
import com.bocoo.pay.mapper.PayAppMapper;
import com.bocoo.pay.mapper.PayOrderExtensionMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayChannelService;
import com.bocoo.pay.service.PayOrderService;
import com.bocoo.pay.service.PayWalletService;
import com.bocoo.pay.service.PaymentSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Payment order service implementation.
 */
@RequiredArgsConstructor
@Service
public class PayOrderServiceImpl implements PayOrderService {

    private static final String DEFAULT_CURRENCY = "CNY";

    private final PayOrderMapper payOrderMapper;
    private final PayOrderExtensionMapper payOrderExtensionMapper;
    private final PaymentSuccessService paymentSuccessService;
    private final PayOrderSupport support;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrder createOrder(PayOrderCreateBo bo) {
        support.validate(bo);
        PayApp app = support.enabledApp(bo.getPayeeTenantId(), bo.getAppKey());
        PayOrder existed = payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
            .eq(PayOrder::getAppId, app.getId())
            .eq(PayOrder::getMerchantOrderId, bo.getMerchantOrderId()), false);
        if (existed != null) {
            return existed;
        }

        PayOrder order = new PayOrder();
        order.setTenantId(bo.getPayerTenantId());
        order.setPayerTenantId(bo.getPayerTenantId());
        order.setPayeeTenantId(bo.getPayeeTenantId());
        order.setAppId(app.getId());
        order.setNo(support.no("PO"));
        order.setMerchantOrderId(bo.getMerchantOrderId());
        order.setSalesDocumentId(bo.getSalesDocumentId());
        order.setSalesOrderNo(bo.getSalesOrderNo());
        order.setMerchantId(bo.getMerchantId());
        order.setMerchantName(bo.getMerchantName());
        order.setCustomerId(bo.getCustomerId());
        order.setCustomerName(bo.getCustomerName());
        order.setSubject(bo.getSubject());
        order.setBody(bo.getBody());
        order.setNotifyUrl(bo.getNotifyUrl());
        order.setPrice(bo.getPrice());
        order.setRefundPrice(0L);
        order.setCurrency(StringUtils.blankToDefault(bo.getCurrency(), DEFAULT_CURRENCY));
        order.setChannelFeeRate(0);
        order.setChannelFeePrice(0L);
        order.setStatus(PayOrderStatus.WAITING.getStatus());
        order.setUserId(bo.getUserId());
        order.setUserType(bo.getUserType());
        order.setExpireTime(bo.getExpireTime());
        payOrderMapper.insert(order);
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PaySubmitVo submitOrder(PaySubmitBo bo) {
        PayOrder order = payOrderMapper.selectById(bo.getOrderId());
        if (order == null) {
            throw new ServiceException("Payment order does not exist");
        }
        if (!PayOrderStatus.isWaiting(order.getStatus())) {
            throw new ServiceException("Payment order is not waiting for payment");
        }
        PayChannel channel = support.enabledChannel(order, bo.getChannelCode());
        if (!PayChannelCode.MOCK.getCode().equals(channel.getCode())
            && !PayChannelCode.WALLET.getCode().equals(channel.getCode())) {
            throw new ServiceException("Real payment channel is TODO until merchant config and webhook are ready");
        }

        PayOrderExtension extension = new PayOrderExtension();
        extension.setTenantId(order.getTenantId());
        extension.setPayerTenantId(order.getPayerTenantId());
        extension.setPayeeTenantId(order.getPayeeTenantId());
        extension.setNo(support.no("PE"));
        extension.setOrderId(order.getId());
        extension.setChannelId(channel.getId());
        extension.setChannelCode(channel.getCode());
        extension.setUserIp(bo.getUserIp());
        extension.setStatus(PayOrderStatus.WAITING.getStatus());
        extension.setChannelExtras(StringUtils.blankToDefault(bo.getChannelExtras(), "{}"));
        payOrderExtensionMapper.insert(extension);

        order.setChannelId(channel.getId());
        order.setChannelCode(channel.getCode());
        order.setExtensionId(extension.getId());
        payOrderMapper.updateById(order);

        if (PayChannelCode.MOCK.getCode().equals(channel.getCode())) {
            notifyOrderSuccess(channel.getCode(), extension.getNo(), "{\"channel\":\"mock\"}");
        } else if (PayChannelCode.WALLET.getCode().equals(channel.getCode())) {
            support.submitWallet(order, extension);
        }

        PaySubmitVo result = new PaySubmitVo();
        result.setExtensionId(extension.getId());
        result.setOrderNo(order.getNo());
        result.setChannelCode(channel.getCode());
        result.setDisplayContent(PayChannelCode.MOCK.getCode().equals(channel.getCode()) ? "mock_success" : null);
        result.setRawData("{}");
        return result;
    }

    @Override
    public PayOrder getOrderByNo(String no) {
        return payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
            .eq(PayOrder::getNo, no), false);
    }

    @Override
    public PayOrder getOrderByMerchantOrderId(Long payerTenantId, String merchantOrderId) {
        return payOrderMapper.selectOne(new LambdaQueryWrapper<PayOrder>()
            .eq(PayOrder::getPayerTenantId, payerTenantId)
            .eq(PayOrder::getMerchantOrderId, merchantOrderId), false);
    }

    @Override
    public List<PayOrderExtension> getAttempts(Long payOrderId) {
        return payOrderExtensionMapper.selectList(new LambdaQueryWrapper<PayOrderExtension>()
            .eq(PayOrderExtension::getOrderId, payOrderId)
            .orderByDesc(PayOrderExtension::getId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean notifyOrderSuccess(String channelCode, String channelOrderNo, String rawNotifyData) {
        PayOrderExtension extension = payOrderExtensionMapper.selectOne(new LambdaQueryWrapper<PayOrderExtension>()
            .eq(PayOrderExtension::getChannelCode, channelCode)
            .eq(PayOrderExtension::getNo, channelOrderNo), false);
        if (extension == null) {
            throw new ServiceException("Payment extension does not exist");
        }

        PayOrder order = payOrderMapper.selectById(extension.getOrderId());
        if (order == null) throw new ServiceException("Payment order does not exist");
        return paymentSuccessService.confirm(extension.getId(), channelOrderNo, order.getPrice(),
            order.getCurrency(), TimeUtils.utcNow(), rawNotifyData);
    }

}
