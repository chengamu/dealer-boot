package com.bocoo.dealer.payment;

import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.pay.domain.bo.CreditOccupyBo;
import com.bocoo.pay.domain.bo.PayBankSubmitBo;
import com.bocoo.pay.domain.entity.MerchantCreditAccount;
import com.bocoo.pay.domain.entity.MerchantReceivable;
import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;
import com.bocoo.pay.domain.vo.PayPalCheckoutVo;
import com.bocoo.pay.domain.vo.CreditAccountSummaryVo;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.domain.vo.PayOrderStatusVo;
import com.bocoo.pay.service.MerchantCreditService;
import com.bocoo.pay.service.PayBankPaymentService;
import com.bocoo.pay.service.PayOrderService;
import com.bocoo.pay.service.PayPalPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesPaymentServiceImpl implements SalesPaymentService {
    private final SalesPaymentOrderLinker linker;
    private final MerchantCreditTermsReader creditTermsReader;
    private final PayOrderService orderService;
    private final PayPalPaymentService payPalService;
    private final PayBankPaymentService bankService;
    private final MerchantCreditService creditService;

    @Override
    public SalesPaymentVo getPayment(Long salesDocumentId) {
        SalesPaymentSnapshot snapshot = linker.getExisting(salesDocumentId);
        SalesDocument document = snapshot.document();
        PayOrder payOrder = snapshot.payOrder();
        List<PayOrderExtension> attempts = LoginHelper.isPlatformTenant()
            ? TenantContextHolder.callWithIgnore(() -> orderService.getAttempts(payOrder.getId()))
            : orderService.getAttempts(payOrder.getId());
        MerchantCreditAccount credit = null;
        try {
            credit = creditService.getAccount(document.getTenantId());
        } catch (RuntimeException ignored) {
            // Credit is optional until the first configured use.
        }
        return SalesPaymentVo.builder().salesDocumentId(document.getSalesDocumentId())
            .orderNo(document.getOrderNo()).tenantId(document.getTenantId())
            .customerName(document.getCustomerName()).projectName(document.getProjectName())
            .totalAmount(document.getTotalAmount()).currency(document.getCurrencyCode())
            .documentStatus(document.getDocumentStatus()).paymentStatus(document.getPaymentStatus())
            .payOrder(PayOrderStatusVo.from(payOrder)).attempts(attempts.stream().map(PayAttemptVo::from).toList())
            .creditAccount(CreditAccountSummaryVo.from(credit)).build();
    }

    @Override
    public PayPalCheckoutVo createPayPal(Long salesDocumentId) {
        return payPalService.create(linker.getOrCreate(salesDocumentId).payOrder().getId());
    }

    @Override
    public PayPalCheckoutVo capturePayPal(Long salesDocumentId, String paypalOrderId) {
        return payPalService.capture(linker.getOrCreate(salesDocumentId).payOrder().getId(), paypalOrderId);
    }

    @Override
    public PayOrderExtension submitBank(Long salesDocumentId, PayBankSubmitBo bo) {
        return bankService.submit(linker.getOrCreate(salesDocumentId).payOrder().getId(), bo);
    }

    @Override
    public MerchantReceivable useCredit(Long salesDocumentId) {
        SalesPaymentSnapshot snapshot = linker.getOrCreate(salesDocumentId);
        MerchantCreditTerms terms = creditTermsReader.read(snapshot.document().getTenantId());
        CreditOccupyBo bo = new CreditOccupyBo();
        bo.setPayOrderId(snapshot.payOrder().getId());
        bo.setSalesDocumentId(salesDocumentId);
        bo.setMerchantId(terms.merchantId());
        bo.setMerchantName(terms.merchantName());
        bo.setConfiguredCreditLimit(terms.creditLimit());
        bo.setCreditTermDays(terms.creditTermDays());
        return creditService.occupy(bo);
    }
}
