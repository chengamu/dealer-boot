package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;

import java.util.List;

record QuickOrderItemWriteResult(List<QuickOrderItem> items, String currencyCode) {
}
