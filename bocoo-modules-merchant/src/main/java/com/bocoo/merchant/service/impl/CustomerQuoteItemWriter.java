package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.merchant.domain.bo.CustomerQuoteItemBo;
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;
import com.bocoo.merchant.mapper.CustomerQuoteItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
class CustomerQuoteItemWriter extends MerchantServiceSupport {

    private final CustomerQuoteItemMapper itemMapper;
    private final CustomerQuoteCalculator calculator;
    private final CustomerQuotePricingSessionFactory sessionFactory;

    CustomerQuoteWriteResult replace(Long quoteId, Long tenantId, List<CustomerQuoteItemBo> rows) {
        return replace(quoteId, tenantId, rows, sessionFactory.create(tenantId));
    }

    CustomerQuoteWriteResult replace(Long quoteId, Long tenantId, List<CustomerQuoteItemBo> rows,
                                     CustomerQuotePricingSession session) {
        itemMapper.delete(this.<CustomerQuoteItem>activeQuery()
            .eq("tenant_id", tenantId).eq("quote_id", quoteId));
        BigDecimal productTotal = BigDecimal.ZERO;
        BigDecimal shippingTotal = BigDecimal.ZERO;
        String currencyCode = null;
        boolean allPassed = rows != null && !rows.isEmpty();
        List<CustomerQuoteItem> items = new java.util.ArrayList<>();
        for (int index = 0; rows != null && index < rows.size(); index++) {
            CustomerQuoteItemBo bo = rows.get(index);
            CustomerQuoteItem item;
            try {
                CustomerQuoteCalculatedItem calculated = calculator.calculate(bo, session);
                if (StringUtils.isNotBlank(currencyCode) && !currencyCode.equals(calculated.currencyCode())) {
                    throw ServiceException.ofMessageKey("customer.quote.currency.mixed");
                }
                currencyCode = calculated.currencyCode();
                item = calculated.item();
                productTotal = productTotal.add(item.getProductAmount());
                shippingTotal = shippingTotal.add(item.getShippingAmount());
            } catch (RuntimeException e) {
                allPassed = false;
                item = calculator.failed(bo, e.getMessage(), session);
            }
            item.setQuoteItemId(null);
            item.setQuoteId(quoteId);
            item.setTenantId(tenantId);
            item.setLineNo(index + 1);
            item.setSortOrder(bo.getSortOrder() == null ? (index + 1) * 10 : bo.getSortOrder());
            items.add(item);
        }
        if (!items.isEmpty() && !itemMapper.insertBatch(items)) {
            throw new IllegalStateException("Customer quote items could not be saved");
        }
        BigDecimal product = money(productTotal);
        BigDecimal shipping = money(shippingTotal);
        CustomerQuoteTotals totals = new CustomerQuoteTotals(currencyCode, product, shipping,
            money(product.add(shipping)), allPassed);
        return new CustomerQuoteWriteResult(totals, List.copyOf(items));
    }

    List<CustomerQuoteItemBo> currentRows(Long quoteId, Long tenantId) {
        QueryWrapper<CustomerQuoteItem> query = this.<CustomerQuoteItem>activeQuery()
            .eq("tenant_id", tenantId).eq("quote_id", quoteId).orderByAsc("line_no", "sort_order");
        return itemMapper.selectList(query).stream().map(this::toBo).toList();
    }

    private CustomerQuoteItemBo toBo(CustomerQuoteItem item) {
        CustomerQuoteItemBo bo = new CustomerQuoteItemBo();
        bo.setQuoteItemId(item.getQuoteItemId());
        bo.setRoomLocation(item.getRoomLocation());
        bo.setSaleProductId(item.getSaleProductId());
        bo.setOrderWidthInch(item.getOrderWidthInch());
        bo.setOrderHeightInch(item.getOrderHeightInch());
        bo.setQuantity(item.getQuantity());
        bo.setSelectedOptionValues(calculator.toVo(item).getSelectedOptionValues());
        bo.setSortOrder(item.getSortOrder());
        bo.setRemark(item.getRemark());
        return bo;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
