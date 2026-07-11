package com.bocoo.merchant.service;

import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;

import java.util.List;

public record CustomerQuoteConversionSnapshot(CustomerQuote quote, List<CustomerQuoteItem> items) {
}
