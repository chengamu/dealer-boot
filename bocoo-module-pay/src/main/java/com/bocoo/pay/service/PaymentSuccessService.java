package com.bocoo.pay.service;

import java.time.LocalDateTime;

public interface PaymentSuccessService {

    boolean confirm(Long extensionId, String channelOrderNo, Long paidPrice,
                    String currency, LocalDateTime paidTime, String safeNotifySummary);

    boolean repair(Long payOrderId);
}
