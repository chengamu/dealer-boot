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
    private final CustomerQuoteDraftAssembler draftAssembler;
    private final CustomerQuotePricingSessionFactory sessionFactory;
    private final SalesOwnershipResolver ownershipResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerQuoteVo insert(CustomerQuoteBo bo) {
        CustomerQuote quote = headerNormalizer.newQuote(bo);
        quoteMapper.insert(quote);
        CustomerQuoteWriteResult result = itemWriter.replace(quote.getQuoteId(), quote.getTenantId(), bo.getItems());
        applyTotals(quote, result.totals());
        quoteMapper.updateById(quote);
        return draftAssembler.assemble(quote, result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CustomerQuoteVo update(CustomerQuoteBo bo) {
        CustomerQuote current = loadDraft(bo.getQuoteId());
        CustomerQuote quote = headerNormalizer.updateQuote(current, bo);
        CustomerQuoteWriteResult result = itemWriter.replace(quote.getQuoteId(), quote.getTenantId(), bo.getItems());
        applyTotals(quote, result.totals());
        if (quoteMapper.updateById(quote) != 1) throw ServiceException.ofMessageKey("customer.quote.notFound");
        return draftAssembler.assemble(quote, result);
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
            .eq("tenant_id", tenantId)
            .eq("business_origin", ownershipResolver.currentBusinessOrigin())
            .in("quote_id", Arrays.asList(ids))) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long copy(Long id) {
        CustomerQuote source = load(id);
        CustomerQuoteBo bo = new CustomerQuoteBo();
        bo.setCustomerId(source.getCustomerId());
        bo.setProjectName(source.getProjectName());
        bo.setCustomerPoNo(source.getCustomerPoNo());
        bo.setRecipientName(source.getRecipientName());
        bo.setRecipientPhone(source.getRecipientPhone());
        bo.setShippingAddress(source.getShippingAddress());
        bo.setQuoteLanguage(source.getQuoteLanguage());
        bo.setValidUntil(source.getValidUntil());
        bo.setRemark(source.getRemark());
        var rows = itemWriter.currentRows(id, source.getTenantId());
        rows.forEach(row -> row.setQuoteItemId(null));
        bo.setItems(rows);
        return insert(bo).getQuoteId();
    }

    @Override
    public CustomerQuoteItemVo calculateItem(CustomerQuoteItemBo bo, String quoteLanguage) {
        CustomerQuotePricingSession session = sessionFactory.create(currentTenantId());
        return calculator.toVo(calculator.calculate(bo, session).item());
    }

    private void applyTotals(CustomerQuote quote, CustomerQuoteTotals totals) {
        quote.setCurrencyCode(totals.currencyCode());
        quote.setProductAmount(totals.productAmount());
        quote.setShippingAmount(totals.shippingAmount());
        quote.setTotalAmount(totals.totalAmount());
    }

    private CustomerQuote loadDraft(Long id) {
        CustomerQuote quote = load(id);
        if (!"DRAFT".equals(quote.getStatus())) {
            throw ServiceException.ofMessageKey("customer.quote.draftOnly");
        }
        return quote;
    }

    private CustomerQuote load(Long id) {
        CustomerQuote quote = quoteMapper.selectOne(this.<CustomerQuote>activeQuery()
            .eq("tenant_id", currentTenantId())
            .eq("business_origin", ownershipResolver.currentBusinessOrigin())
            .eq("quote_id", id), false);
        if (quote == null) throw ServiceException.ofMessageKey("customer.quote.notFound");
        return quote;
    }
}
