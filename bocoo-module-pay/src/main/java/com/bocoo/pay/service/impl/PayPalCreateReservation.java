package com.bocoo.pay.service.impl;

import com.bocoo.pay.domain.entity.PayOrder;
import com.bocoo.pay.domain.entity.PayOrderExtension;

record PayPalCreateReservation(PayOrder order, PayOrderExtension attempt) {
}
