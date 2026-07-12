package com.bocoo.dealer.payment;

import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.pay.domain.entity.PayOrder;

record SalesPaymentSnapshot(SalesDocument document, PayOrder payOrder) {
}
