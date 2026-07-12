package com.bocoo.merchant.service.impl;

import com.bocoo.merchant.domain.entity.CustomerQuoteItem;

import java.util.List;

record CustomerQuoteWriteResult(CustomerQuoteTotals totals, List<CustomerQuoteItem> items) { }
