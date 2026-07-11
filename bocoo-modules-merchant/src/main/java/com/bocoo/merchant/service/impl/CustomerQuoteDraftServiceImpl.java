package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.CustomerQuoteDraftService;
import com.bocoo.merchant.service.CustomerQuoteQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CustomerQuoteDraftServiceImpl extends MerchantServiceSupport implements CustomerQuoteDraftService {

    private final CustomerQuoteMapper quoteMapper;
    private final CustomerQuoteItemMapper itemMapper;
    private final CustomerQuoteHeaderNormalizer headerNormalizer;
    private final CustomerQuoteItemWriter itemWriter;
    private final CustomerQuoteCalculator calculator;
    private final CustomerQuoteQueryService queryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insert(CustomerQuoteBo bo) {
        CustomerQuote quote = headerNormalizer.newQuote(bo);
        quoteMapper.insert(quote);
        CustomerQuoteTotals totals = itemWriter.replace(quote.getQuoteId(), quote.getTenantId(), bo.getItems());
        quote.setCurrencyCode(totals.currencyCode());
        quote.setProductAmount(totals.productAmount());
        quote.setShippingAmount(totals.shippingAmount());
        quote.setTotalAmount(totals.totalAmount());
        quoteMapper.updateById(quote);
        return quote.getQuoteId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CustomerQuoteBo bo) {
        CustomerQuote current = loadDraft(bo.getQuoteId());
        CustomerQuote quote = headerNormalizer.updateQuote(current, bo);
        CustomerQuoteTotals totals = itemWriter.replace(quote.getQuoteId(), quote.getTenantId(), bo.getItems());
        quote.setCurrencyCode(totals.currencyCode());
        quote.setProductAmount(totals.productAmount());
        quote.setShippingAmount(totals.shippingAmount());
        quote.setTotalAmount(totals.totalAmount());
        return quoteMapper.updateById(quote) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(Long[] ids) {
        Long tenantId = currentTenantId();
        for (Long id : Arrays.stream(ids).distinct().toList()) {
            loadDraft(id);
            itemMapper.delete(this.<CustomerQuoteItem>activeQuery()
                .eq("tenant_id", tenantId).eq("quote_id", id));
        }
        return quoteMapper.delete(this.<CustomerQuote>activeQuery()
            .eq("tenant_id", tenantId).in("quote_id", Arrays.asList(ids))) > 0;
    }

    @Override
    public CustomerQuoteItemVo calculateItem(CustomerQuoteItemBo bo, String quoteLanguage) {
        return calculator.toVo(calculator.calculate(bo).item());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerQuoteVo calculateAll(Long id) {
        CustomerQuote quote = loadDraft(id);
        CustomerQuoteTotals totals = itemWriter.replace(id, quote.getTenantId(),
            itemWriter.currentRows(id, quote.getTenantId()));
        quote.setCurrencyCode(totals.currencyCode());
        quote.setProductAmount(totals.productAmount());
        quote.setShippingAmount(totals.shippingAmount());
        quote.setTotalAmount(totals.totalAmount());
        quoteMapper.updateById(quote);
        return queryService.queryById(id);
    }

    private CustomerQuote loadDraft(Long id) {
        CustomerQuote quote = quoteMapper.selectOne(this.<CustomerQuote>activeQuery()
            .eq("tenant_id", currentTenantId()).eq("quote_id", id), false);
        if (quote == null) {
            throw ServiceException.ofMessageKey("customer.quote.notFound");
        }
        if (!"DRAFT".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.draftOnly");
        }
        return quote;
    }
}
