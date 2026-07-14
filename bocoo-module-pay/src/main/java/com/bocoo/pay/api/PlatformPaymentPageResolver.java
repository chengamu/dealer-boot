package com.bocoo.pay.api;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.pay.domain.bo.BankTransferQueryBo;
import com.bocoo.pay.domain.bo.PaymentOrderQueryBo;

/** Resolves platform payment pages inside the sales document data scope. */
public interface PlatformPaymentPageResolver {
    PaymentScopePage pagePaymentOrderIds(PaymentOrderQueryBo query, PageQuery pageQuery);

    PaymentScopePage pageBankTransferIds(BankTransferQueryBo query, PageQuery pageQuery);
}
