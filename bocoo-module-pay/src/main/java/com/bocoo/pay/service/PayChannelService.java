package com.bocoo.pay.service;

import com.bocoo.pay.domain.entity.PayChannel;

/**
 * Payment channel service contract.
 */
public interface PayChannelService {

    PayChannel getEnabledChannel(Long payeeTenantId, Long appId, String channelCode);

    Object parseConfig(String channelCode, String config);
}
