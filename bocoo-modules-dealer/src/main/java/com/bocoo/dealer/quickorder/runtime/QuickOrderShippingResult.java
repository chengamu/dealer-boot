package com.bocoo.dealer.quickorder.runtime;

import java.math.BigDecimal;

record QuickOrderShippingResult(Long templateId, String templateCode, Long ruleId,
                                String feeCode, BigDecimal unitAmount) {
}
