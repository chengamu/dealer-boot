package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.lock.annotation.Lock4j;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.CustomerQuoteLifecycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerQuoteLifecycleServiceImpl extends MerchantServiceSupport implements CustomerQuoteLifecycleService {

    private final CustomerQuoteMapper quoteMapper;
    private final CustomerQuoteItemWriter itemWriter;
    private final CustomerQuoteCalculator calculator;

    @Override
    @Lock4j(name = "customer-quote-confirm", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class, noRollbackFor = ServiceException.class)
    public Boolean confirm(Long id) {
        CustomerQuote quote = load(id);
        if (!"DRAFT".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.confirm.draftOnly");
        }
        List<CustomerQuoteItemBo> rows = itemWriter.currentRows(id, quote.getTenantId());
        if (rows.isEmpty()) {
            throw ServiceException.ofMessageKey("customer.quote.item.required");
        }
        CustomerQuoteTotals totals = itemWriter.replace(id, quote.getTenantId(), rows);
        quote.setCurrencyCode(totals.currencyCode());
        quote.setProductAmount(totals.productAmount());
        quote.setShippingAmount(totals.shippingAmount());
        quote.setTotalAmount(totals.totalAmount());
        quoteMapper.updateById(quote);
        if (!totals.allPassed()) {
            throw ServiceException.ofMessageKey("customer.quote.calculation.required");
        }
        if ("EN_US".equals(quote.getQuoteLanguage()) && rows.stream().anyMatch(this::englishIncomplete)) {
            throw ServiceException.ofMessageKey("customer.quote.english.incomplete");
        }
        Long userId = LoginHelper.getUserId();
        String username = LoginHelper.getUsername();
        LocalDateTime confirmedTime = TimeUtils.utcNow();
        boolean updated = quoteMapper.update(null, new UpdateWrapper<CustomerQuote>()
            .eq("quote_id", id)
            .eq("tenant_id", quote.getTenantId())
            .eq("status", "DRAFT")
            .set("status", "CONFIRMED")
            .set("confirmed_by_id", userId)
            .set("confirmed_by", username)
            .set("confirmed_time", confirmedTime)) > 0;
        if (!updated) {
            throw ServiceException.ofMessageKey("customer.quote.confirm.draftOnly");
        }
        quote.setStatus("CONFIRMED");
        quote.setConfirmedById(userId);
        quote.setConfirmedBy(username);
        quote.setConfirmedTime(confirmedTime);
        return Boolean.TRUE;
    }

    @Override
    @Lock4j(name = "customer-quote-void", keys = {"#id"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean voidQuote(Long id) {
        CustomerQuote quote = load(id);
        if (!"CONFIRMED".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.void.confirmedOnly");
        }
        if (quote.getSalesDocumentId() != null) {
            throw ServiceException.ofMessageKey("customer.quote.void.convertedDenied");
        }
        boolean updated = quoteMapper.update(null, new UpdateWrapper<CustomerQuote>()
            .eq("quote_id", id)
            .eq("tenant_id", quote.getTenantId())
            .eq("status", "CONFIRMED")
            .isNull("sales_document_id")
            .set("status", "VOID")) > 0;
        if (!updated) {
            throw ServiceException.ofMessageKey("customer.quote.void.confirmedOnly");
        }
        quote.setStatus("VOID");
        return Boolean.TRUE;
    }

    private boolean englishIncomplete(CustomerQuoteItemBo row) {
        return !calculator.optionSnapshot(row.getSaleProductId(), row.getSelectedOptionValues()).englishComplete();
    }

    private CustomerQuote load(Long id) {
        CustomerQuote quote = quoteMapper.selectOne(this.<CustomerQuote>activeQuery()
            .eq("tenant_id", currentTenantId()).eq("quote_id", id), false);
        if (quote == null) {
            throw ServiceException.ofMessageKey("customer.quote.notFound");
        }
        return quote;
    }
}
