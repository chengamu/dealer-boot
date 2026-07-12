package com.bocoo.dealer.quickorder.domain.vo;

import java.math.BigDecimal;

public record QuickOrderSubmitResultVo(Long salesDocumentId, String orderNo, BigDecimal totalAmount) {
}
