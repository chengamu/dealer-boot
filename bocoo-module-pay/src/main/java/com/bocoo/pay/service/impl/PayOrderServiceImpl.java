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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment order service implementation.
 */
@RequiredArgsConstructor
@Service
public class PayOrderServiceImpl implements PayOrderService {

    private static final String DEFAULT_CURRENCY = "CNY";

    private final PayAppMapper payAppMapper;
    private final PayOrderMapper payOrderMapper;
    private final PayOrderExtensionMapper payOrderExtensionMapper;
    private final PayChannelService payChannelService;
    private final PayWalletService payWalletService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PayOrder createOrder(PayOrderCreateBo bo) {
        validateCreateOrder(bo);
        PayApp app = getEnabledApp(bo.getPayeeTenantId(), bo.getAppKey());
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
        order.setNo(buildNo("PO"));
        order.setMerchantOrderId(bo.getMerchantOrderId());
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
        PayChannel channel = payChannelService.getEnabledChannel(order.getPayeeTenantId(), order.getAppId(), bo.getChannelCode());
        if (!PayChannelCode.MOCK.getCode().equals(channel.getCode())
            && !PayChannelCode.WALLET.getCode().equals(channel.getCode())) {
            throw new ServiceException("Real payment channel is TODO until merchant config and webhook are ready");
        }

        PayOrderExtension extension = new PayOrderExtension();
        extension.setTenantId(order.getTenantId());
        extension.setPayerTenantId(order.getPayerTenantId());
        extension.setPayeeTenantId(order.getPayeeTenantId());
        extension.setNo(buildNo("PE"));
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
            submitWalletPayment(order, extension);
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
    @Transactional(rollbackFor = Exception.class)
    public boolean notifyOrderSuccess(String channelCode, String channelOrderNo, String rawNotifyData) {
        PayOrderExtension extension = payOrderExtensionMapper.selectOne(new LambdaQueryWrapper<PayOrderExtension>()
            .eq(PayOrderExtension::getChannelCode, channelCode)
            .eq(PayOrderExtension::getNo, channelOrderNo), false);
        if (extension == null) {
            throw new ServiceException("Payment extension does not exist");
        }

        int extensionRows = payOrderExtensionMapper.update(null, new LambdaUpdateWrapper<PayOrderExtension>()
            .set(PayOrderExtension::getStatus, PayOrderStatus.SUCCESS.getStatus())
            .set(PayOrderExtension::getChannelNotifyData, rawNotifyData)
            .eq(PayOrderExtension::getId, extension.getId())
            .eq(PayOrderExtension::getStatus, PayOrderStatus.WAITING.getStatus()));

        int orderRows = payOrderMapper.update(null, new LambdaUpdateWrapper<PayOrder>()
            .set(PayOrder::getStatus, PayOrderStatus.SUCCESS.getStatus())
            .set(PayOrder::getSuccessTime, TimeUtils.utcNow())
            .set(PayOrder::getChannelOrderNo, channelOrderNo)
            .eq(PayOrder::getId, extension.getOrderId())
            .eq(PayOrder::getStatus, PayOrderStatus.WAITING.getStatus()));
        return extensionRows > 0 || orderRows > 0;
    }

    private PayApp getEnabledApp(Long payeeTenantId, String appKey) {
        Long tenantId = payeeTenantId == null ? TenantConstants.PLATFORM_TENANT_ID : payeeTenantId;
        PayApp app = payAppMapper.selectOne(new LambdaQueryWrapper<PayApp>()
            .eq(PayApp::getTenantId, tenantId)
            .eq(PayApp::getAppKey, appKey)
            .eq(PayApp::getStatus, "1"), false);
        if (app == null) {
            throw new ServiceException("Payment app is not configured or disabled");
        }
        return app;
    }

    private void submitWalletPayment(PayOrder order, PayOrderExtension extension) {
        if (order.getUserId() == null || StringUtils.isBlank(order.getUserType())) {
            throw new ServiceException("Wallet payment user is required");
        }
        PayWallet wallet = payWalletService.getOrCreateWallet(order.getPayerTenantId(), order.getUserId(), order.getUserType());
        PayWalletChangeBo changeBo = new PayWalletChangeBo();
        changeBo.setWalletId(wallet.getId());
        changeBo.setPrice(-order.getPrice());
        changeBo.setTitle(order.getSubject());
        changeBo.setBizType(PayWalletBizType.PAYMENT.getType());
        changeBo.setBizId(order.getNo());
        changeBo.setRemark(order.getMerchantOrderId());
        payWalletService.changeBalance(changeBo);
        notifyOrderSuccess(PayChannelCode.WALLET.getCode(), extension.getNo(), "{\"channel\":\"wallet\"}");
    }

    private void validateCreateOrder(PayOrderCreateBo bo) {
        if (bo.getPayerTenantId() == null || bo.getPayerTenantId() <= 0) {
            throw new ServiceException("Payer tenant is required");
        }
        if (StringUtils.isBlank(bo.getAppKey())) {
            throw new ServiceException("Payment app key is required");
        }
        if (StringUtils.isBlank(bo.getMerchantOrderId())) {
            throw new ServiceException("Merchant order id is required");
        }
        if (StringUtils.isBlank(bo.getSubject())) {
            throw new ServiceException("Payment subject is required");
        }
        if (bo.getPrice() == null || bo.getPrice() < 0) {
            throw new ServiceException("Payment price is invalid");
        }
        if (bo.getPayeeTenantId() == null) {
            bo.setPayeeTenantId(TenantConstants.PLATFORM_TENANT_ID);
        }
    }

    private String buildNo(String prefix) {
        return prefix + TimeUtils.utcNow().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
            + IdWorker.getId();
    }
}
