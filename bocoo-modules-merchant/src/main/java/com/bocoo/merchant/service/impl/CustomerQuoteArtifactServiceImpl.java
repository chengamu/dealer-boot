package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mail.utils.MailUtils;
import com.bocoo.merchant.domain.bo.CustomerQuoteEmailBo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.service.CustomerQuoteArtifactService;
import com.bocoo.merchant.service.CustomerQuoteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.nio.file.Files;

@Service
@RequiredArgsConstructor
public class CustomerQuoteArtifactServiceImpl implements CustomerQuoteArtifactService {
    private final CustomerQuoteQueryService queryService;
    private final CustomerQuotePdfRenderer renderer;

    @Override
    public byte[] pdf(Long quoteId) {
        return renderer.render(confirmed(quoteId));
    }

    @Override
    public String sendEmail(Long quoteId, CustomerQuoteEmailBo bo) {
        CustomerQuoteVo quote = confirmed(quoteId);
        java.io.File file = null;
        try {
            file = java.io.File.createTempFile("quotation-" + quoteId + "-", ".pdf");
            Files.write(file.toPath(), renderer.render(quote));
            String subject = StringUtils.blankToDefault(bo.getSubject(), "Quotation " + quote.getQuoteNo());
            String message = HtmlUtils.htmlEscape(StringUtils.blankToDefault(
                bo.getMessage(), "Please find the attached quotation.")).replace("\n", "<br>");
            return MailUtils.sendHtml(bo.getRecipient(), subject, "<p>" + message + "</p>", file);
        } catch (RuntimeException | java.io.IOException e) {
            throw new ServiceException(e.getMessage());
        } finally {
            if (file != null) try { Files.deleteIfExists(file.toPath()); } catch (java.io.IOException ignored) { }
        }
    }

    private CustomerQuoteVo confirmed(Long quoteId) {
        CustomerQuoteVo quote = queryService.queryById(quoteId);
        if (!"CONFIRMED".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.export.confirmedOnly");
        }
        return quote;
    }
}
