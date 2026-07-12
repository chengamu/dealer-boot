package com.bocoo.pay.service;

import com.bocoo.pay.domain.vo.BankCollectionAccountVo;
import com.bocoo.pay.domain.vo.EnabledPayChannelVo;

import java.util.List;

public interface PayChannelQueryService {
    List<EnabledPayChannelVo> enabledChannels(Long payOrderId, Long appId);

    List<BankCollectionAccountVo> bankAccounts(Long payOrderId, Long appId, String currency);
}
