package com.bocoo.merchant.service;

public interface CustomerQuoteConversionSupport {
    CustomerQuoteConversionSnapshot load(Long quoteId);

    boolean markConverted(Long quoteId, Long salesDocumentId, String orderNo);
}
