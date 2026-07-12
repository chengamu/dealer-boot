package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.constant.TenantConstants;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.pay.domain.bo.PayOrderCreateBo;
import com.bocoo.pay.domain.bo.PayWalletChangeBo;
import com.bocoo.pay.domain.entity.PayApp;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.entity.PayWallet;
import com.bocoo.pay.enums.PayWalletBizType;
import com.bocoo.pay.mapper.PayAppMapper;
import com.bocoo.pay.service.PayChannelService;
import com.bocoo.pay.service.PayWalletService;
import com.bocoo.pay.service.PaymentSuccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
class PayOrderSupport {
    private final PayAppMapper appMapper;
    private final PayChannelService channelService;
    private final PayWalletService walletService;
    private final PaymentSuccessService successService;

    PayApp enabledApp(Long payeeTenantId, String appKey) {
        Long tenantId = payeeTenantId == null ? TenantConstants.PLATFORM_TENANT_ID : payeeTenantId;
        PayApp app = TenantContextHolder.callWithIgnore(() -> appMapper.selectOne(new LambdaQueryWrapper<PayApp>()
            .eq(PayApp::getTenantId, tenantId).eq(PayApp::getAppKey, appKey).eq(PayApp::getStatus, "1"), false));
        if (app == null) throw new ServiceException("Payment app is not configured or disabled");
        return app;
    }

    PayChannel enabledChannel(PayOrder order, String channelCode) {
        return channelService.getEnabledChannel(order.getPayeeTenantId(), order.getAppId(), channelCode);
    }

    void submitWallet(PayOrder order, PayOrderExtension extension) {
        if (order.getUserId() == null || StringUtils.isBlank(order.getUserType())) {
            throw new ServiceException("Wallet payment user is required");
        }
        PayWallet wallet = walletService.getOrCreateWallet(order.getPayerTenantId(), order.getUserId(), order.getUserType());
        PayWalletChangeBo change = new PayWalletChangeBo();
        change.setWalletId(wallet.getId());
        change.setPrice(-order.getPrice());
        change.setTitle(order.getSubject());
        change.setBizType(PayWalletBizType.PAYMENT.getType());
        change.setBizId(order.getNo());
        change.setRemark(order.getMerchantOrderId());
        walletService.changeBalance(change);
        successService.confirm(extension.getId(), extension.getNo(), order.getPrice(), order.getCurrency(),
            TimeUtils.utcNow(), "wallet_payment_completed");
    }

    void validate(PayOrderCreateBo bo) {
        if (bo.getPayerTenantId() == null || bo.getPayerTenantId() <= 0) throw new ServiceException("Payer tenant is required");
        if (StringUtils.isBlank(bo.getAppKey())) throw new ServiceException("Payment app key is required");
        if (StringUtils.isBlank(bo.getMerchantOrderId())) throw new ServiceException("Merchant order id is required");
        if (StringUtils.isBlank(bo.getSubject())) throw new ServiceException("Payment subject is required");
        if (bo.getPrice() == null || bo.getPrice() < 0) throw new ServiceException("Payment price is invalid");
        if (bo.getPayeeTenantId() == null) bo.setPayeeTenantId(TenantConstants.PLATFORM_TENANT_ID);
    }

    String no(String prefix) {
        return prefix + TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
            + com.baomidou.mybatisplus.core.toolkit.IdWorker.getId();
    }
}
