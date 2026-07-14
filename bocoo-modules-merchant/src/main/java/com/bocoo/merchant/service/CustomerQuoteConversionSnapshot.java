package com.bocoo.merchant.service;

import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;

import java.util.List;

public record CustomerQuoteConversionSnapshot(CustomerQuote quote, List<CustomerQuoteItem> items) {

    public String businessOrigin() {
        return quote.getBusinessOrigin();
    }

    public Long salesStoreId() {
        return quote.getSalesStoreId();
    }

    public Long deptId() {
        return quote.getDeptId();
    }

    public Long ownerUserId() {
        return quote.getOwnerUserId();
    }
}
