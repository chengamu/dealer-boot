package com.bocoo.pay.service;

import com.bocoo.pay.domain.bo.PayWalletChangeBo;
import com.bocoo.pay.domain.entity.PayWallet;
import com.bocoo.pay.domain.entity.PayWalletTransaction;

/**
 * Payment wallet service contract.
 */
public interface PayWalletService {

    PayWallet getOrCreateWallet(Long tenantId, Long userId, String userType);

    PayWalletTransaction changeBalance(PayWalletChangeBo bo);
}
