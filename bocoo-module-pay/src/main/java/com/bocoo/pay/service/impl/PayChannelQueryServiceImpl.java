package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.vo.BankCollectionAccountVo;
import com.bocoo.pay.domain.vo.EnabledPayChannelVo;
import com.bocoo.pay.enums.PayChannelCode;
import com.bocoo.pay.mapper.PayChannelMapper;
import com.bocoo.pay.mapper.PayOrderMapper;
import com.bocoo.pay.service.PayChannelQueryService;
import com.bocoo.pay.service.PayOperatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PayChannelQueryServiceImpl implements PayChannelQueryService {
    private final PayChannelMapper channelMapper;
    private final PayOrderMapper orderMapper;
    private final PayOperatorContext operator;
    private final BankAccountConfigParser bankAccountParser;

    @Override
    public List<EnabledPayChannelVo> enabledChannels(Long payOrderId, Long appId) {
        ChannelScope scope = scope(payOrderId, appId);
        return channels(scope, null).stream().map(EnabledPayChannelVo::from).toList();
    }

    @Override
    public List<BankCollectionAccountVo> bankAccounts(Long payOrderId, Long appId, String currency) {
        ChannelScope scope = scope(payOrderId, appId);
        return channels(scope, PayChannelCode.BANK_TRANSFER.getCode()).stream()
            .flatMap(channel -> bankAccountParser.parse(channel.getConfig(), currency).stream()).toList();
    }

    private List<PayChannel> channels(ChannelScope scope, String code) {
        return TenantContextHolder.callWithIgnore(() -> channelMapper.selectList(new LambdaQueryWrapper<PayChannel>()
            .eq(PayChannel::getTenantId, scope.tenantId()).eq(scope.appId() != null, PayChannel::getAppId, scope.appId())
            .eq(PayChannel::getStatus, "1").eq(code != null, PayChannel::getCode, code)
            .orderByAsc(PayChannel::getCode)));
    }

    private ChannelScope scope(Long payOrderId, Long appId) {
        if (payOrderId == null) {
            if (!operator.isPlatform()) throw new ServiceException("Pay order is required for merchant channel query");
            return new ChannelScope(operator.tenantId(), appId);
        }
        PayOrder order = TenantContextHolder.callWithIgnore(() -> orderMapper.selectById(payOrderId));
        if (order == null) throw new ServiceException("Payment order does not exist");
        Long tenantId = operator.tenantId();
        if (!tenantId.equals(order.getPayerTenantId()) && !tenantId.equals(order.getPayeeTenantId())) {
            throw new ServiceException("Payment order does not belong to the current tenant");
        }
        return new ChannelScope(order.getPayeeTenantId(), order.getAppId());
    }

    private record ChannelScope(Long tenantId, Long appId) {
    }
}
