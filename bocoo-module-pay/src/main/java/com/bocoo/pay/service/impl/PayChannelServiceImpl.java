package com.bocoo.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.pay.domain.entity.PayChannel;
import com.bocoo.pay.mapper.PayChannelMapper;
import com.bocoo.pay.service.PayChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Payment channel service implementation.
 */
@RequiredArgsConstructor
@Service
public class PayChannelServiceImpl implements PayChannelService {

    private final PayChannelMapper payChannelMapper;

    @Override
    public PayChannel getEnabledChannel(Long payeeTenantId, Long appId, String channelCode) {
        PayChannel channel = TenantContextHolder.callWithIgnore(() -> payChannelMapper.selectOne(new LambdaQueryWrapper<PayChannel>()
            .eq(PayChannel::getTenantId, payeeTenantId)
            .eq(PayChannel::getAppId, appId)
            .eq(PayChannel::getCode, channelCode)
            .eq(PayChannel::getStatus, "1"), false));
        if (channel == null) {
            throw new ServiceException("Payment channel is not configured or disabled");
        }
        return channel;
    }

    @Override
    public Object parseConfig(String channelCode, String config) {
        if (StringUtils.isBlank(config)) {
            return Map.of();
        }
        Map<?, ?> parsed = JsonUtils.parseObject(config, Map.class);
        return parsed == null ? Map.of() : parsed;
    }
}
