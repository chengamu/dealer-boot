package com.bocoo.merchant.service;

import com.bocoo.merchant.domain.bo.CustomerQuoteEmailBo;

public interface CustomerQuoteArtifactService {
    byte[] pdf(Long quoteId);

    String sendEmail(Long quoteId, CustomerQuoteEmailBo bo);
}
