package com.bocoo.pay.domain.vo;

import com.bocoo.pay.domain.entity.PayChannel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class EnabledPayChannelVo {
    Long channelId;
    Long appId;
    String channelCode;
    Integer feeRate;
    String remark;

    public static EnabledPayChannelVo from(PayChannel row) {
        return builder().channelId(row.getId()).appId(row.getAppId()).channelCode(row.getCode())
            .feeRate(row.getFeeRate()).remark(row.getRemark()).build();
    }
}
