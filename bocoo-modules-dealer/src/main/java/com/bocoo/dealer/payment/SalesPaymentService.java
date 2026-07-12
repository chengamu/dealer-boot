package com.bocoo.dealer.payment;

import com.bocoo.pay.domain.bo.PayBankSubmitBo;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.vo.PayPalCheckoutVo;

public interface SalesPaymentService {
    SalesPaymentVo getPayment(Long salesDocumentId);

    PayPalCheckoutVo createPayPal(Long salesDocumentId);

    PayPalCheckoutVo capturePayPal(Long salesDocumentId, String paypalOrderId);

    PayOrderExtension submitBank(Long salesDocumentId, PayBankSubmitBo bo);

    MerchantReceivable useCredit(Long salesDocumentId);
}
