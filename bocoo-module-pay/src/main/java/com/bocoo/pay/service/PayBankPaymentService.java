package com.bocoo.pay.service;

import com.bocoo.pay.domain.bo.PayBankReviewBo;
import com.bocoo.pay.domain.bo.PayBankSubmitBo;
import com.bocoo.pay.domain.bo.PaySupplementBo;
import com.bocoo.pay.domain.entity.PayOrderExtension;

public interface PayBankPaymentService {
    PayOrderExtension submit(Long payOrderId, PayBankSubmitBo bo);

    PayOrderExtension review(Long extensionId, PayBankReviewBo bo);

    PayOrderExtension supplement(Long payOrderId, PaySupplementBo bo);
}
