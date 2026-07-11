package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.domain.entity.CustomerQuoteItem;

record CustomerQuoteCalculatedItem(CustomerQuoteItem item, String currencyCode) {
}
