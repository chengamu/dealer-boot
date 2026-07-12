package com.bocoo.pay.service;

import com.bocoo.pay.domain.bo.CreditAdjustBo;
import com.bocoo.pay.domain.bo.CreditFreezeBo;
import com.bocoo.pay.domain.bo.CreditOccupyBo;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantReceivable;

public interface MerchantCreditService {
    MerchantCreditAccount getAccount(Long merchantTenantId);

    MerchantReceivable occupy(CreditOccupyBo bo);

    MerchantReceivable repay(Long receivableId, String reason);

    MerchantCreditAccount adjust(Long accountId, CreditAdjustBo bo);

    MerchantCreditAccount freeze(Long accountId, CreditFreezeBo bo);
}
