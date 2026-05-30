package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayNotifyTask;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayRefund;
import com.bocoo.pay.domain.entity.PayWallet;
import com.bocoo.pay.mapper.PayChannelMapper;
import com.bocoo.pay.mapper.PayNotifyTaskMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.mapper.PayRefundMapper;
import com.bocoo.pay.mapper.PayWalletMapper;
import com.bocoo.pay.service.PayAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Payment management query service implementation.
 */
@RequiredArgsConstructor
@Service
public class PayAdminServiceImpl implements PayAdminService {

    private static final String MASKED_CONFIG = "{\"masked\":true}";

    private final PayChannelMapper payChannelMapper;
    private final PayOrderMapper payOrderMapper;
    private final PayRefundMapper payRefundMapper;
    private final PayNotifyTaskMapper payNotifyTaskMapper;
    private final PayWalletMapper payWalletMapper;

    @Override
    public TableDataInfo<PayChannel> selectChannelPage(PayChannel query, PageQuery pageQuery) {
        checkPlatformTenant();
        Long tenantId = currentTenantId();
        Page<PayChannel> page = TenantContextHolder.callWithIgnore(() -> payChannelMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<PayChannel>()
                .eq(PayChannel::getTenantId, tenantId)
                .eq(query.getAppId() != null, PayChannel::getAppId, query.getAppId())
                .eq(StringUtils.isNotBlank(query.getCode()), PayChannel::getCode, query.getCode())
                .eq(StringUtils.isNotBlank(query.getStatus()), PayChannel::getStatus, query.getStatus())
                .orderByDesc(PayChannel::getCreateTime)));
        page.getRecords().forEach(this::maskChannelConfig);
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<PayOrder> selectOrderPage(PayOrder query, PageQuery pageQuery) {
        Long tenantId = currentTenantId();
        Page<PayOrder> page = TenantContextHolder.callWithIgnore(() -> payOrderMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<PayOrder>()
                .eq(LoginHelper.isPlatformTenant(), PayOrder::getPayeeTenantId, tenantId)
                .eq(!LoginHelper.isPlatformTenant(), PayOrder::getPayerTenantId, tenantId)
                .eq(StringUtils.isNotBlank(query.getNo()), PayOrder::getNo, query.getNo())
                .eq(StringUtils.isNotBlank(query.getMerchantOrderId()), PayOrder::getMerchantOrderId, query.getMerchantOrderId())
                .eq(StringUtils.isNotBlank(query.getChannelCode()), PayOrder::getChannelCode, query.getChannelCode())
                .eq(query.getStatus() != null, PayOrder::getStatus, query.getStatus())
                .orderByDesc(PayOrder::getCreateTime)));
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<PayRefund> selectRefundPage(PayRefund query, PageQuery pageQuery) {
        Long tenantId = currentTenantId();
        Page<PayRefund> page = TenantContextHolder.callWithIgnore(() -> payRefundMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<PayRefund>()
                .eq(LoginHelper.isPlatformTenant(), PayRefund::getPayeeTenantId, tenantId)
                .eq(!LoginHelper.isPlatformTenant(), PayRefund::getPayerTenantId, tenantId)
                .eq(StringUtils.isNotBlank(query.getNo()), PayRefund::getNo, query.getNo())
                .eq(StringUtils.isNotBlank(query.getMerchantOrderId()), PayRefund::getMerchantOrderId, query.getMerchantOrderId())
                .eq(StringUtils.isNotBlank(query.getMerchantRefundId()), PayRefund::getMerchantRefundId, query.getMerchantRefundId())
                .eq(query.getStatus() != null, PayRefund::getStatus, query.getStatus())
                .orderByDesc(PayRefund::getCreateTime)));
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<PayNotifyTask> selectNotifyTaskPage(PayNotifyTask query, PageQuery pageQuery) {
        Long tenantId = currentTenantId();
        Page<PayNotifyTask> page = TenantContextHolder.callWithIgnore(() -> payNotifyTaskMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<PayNotifyTask>()
                .eq(LoginHelper.isPlatformTenant(), PayNotifyTask::getPayeeTenantId, tenantId)
                .eq(!LoginHelper.isPlatformTenant(), PayNotifyTask::getPayerTenantId, tenantId)
                .eq(query.getType() != null, PayNotifyTask::getType, query.getType())
                .eq(query.getStatus() != null, PayNotifyTask::getStatus, query.getStatus())
                .eq(StringUtils.isNotBlank(query.getMerchantOrderId()), PayNotifyTask::getMerchantOrderId, query.getMerchantOrderId())
                .orderByDesc(PayNotifyTask::getCreateTime)));
        return TableDataInfo.build(page);
    }

    @Override
    public TableDataInfo<PayWallet> selectWalletPage(PayWallet query, PageQuery pageQuery) {
        Long tenantId = currentTenantId();
        Page<PayWallet> page = TenantContextHolder.callWithIgnore(() -> payWalletMapper.selectPage(pageQuery.build(),
            new LambdaQueryWrapper<PayWallet>()
                .eq(PayWallet::getTenantId, tenantId)
                .eq(query.getUserId() != null, PayWallet::getUserId, query.getUserId())
                .eq(StringUtils.isNotBlank(query.getUserType()), PayWallet::getUserType, query.getUserType())
                .orderByDesc(PayWallet::getCreateTime)));
        return TableDataInfo.build(page);
    }

    private void maskChannelConfig(PayChannel channel) {
        if (StringUtils.isNotBlank(channel.getConfig())) {
            channel.setConfig(MASKED_CONFIG);
        }
    }

    private void checkPlatformTenant() {
        if (!LoginHelper.isPlatformTenant()) {
            throw new ServiceException("Only platform tenant can manage payment channels");
        }
    }

    private Long currentTenantId() {
        Long tenantId = LoginHelper.getTenantId();
        if (tenantId == null || tenantId <= 0) {
            throw new ServiceException("Tenant is required");
        }
        return tenantId;
    }
}
