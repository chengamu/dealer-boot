package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.bo.SalesDocumentItemBo;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
@RequiredArgsConstructor
class SalesDocumentItemWriter extends DealerServiceSupport {
    private final SalesDocumentItemMapper mapper;
    private final SalesDocumentCalculator calculator;
    private final SalesDocumentJson json;

    SalesTotals replace(Long documentId, Long tenantId, List<SalesDocumentItemBo> rows) {
        mapper.delete(this.<SalesDocumentItem>active().eq("tenant_id", tenantId).eq("sales_document_id", documentId));
        BigDecimal list = BigDecimal.ZERO, product = BigDecimal.ZERO, shipping = BigDecimal.ZERO;
        String currency = null;
        boolean passed = rows != null && !rows.isEmpty();
        for (int index = 0; rows != null && index < rows.size(); index++) {
            SalesDocumentItemBo bo = rows.get(index);
            SalesDocumentItem item;
            try {
                SalesDocumentCalculator.CalculatedLine line = calculator.calculate(bo, tenantId);
                if (currency != null && !currency.equals(line.currencyCode())) {
                    throw ServiceException.ofMessageKey("dealer.sales.mixedCurrency");
                }
                currency = line.currencyCode();
                item = line.item();
                list = list.add(item.getListAmount());
                product = product.add(item.getProductAmount());
                shipping = shipping.add(item.getShippingAmount());
            } catch (RuntimeException e) {
                passed = false;
                item = failed(bo, e.getMessage());
            }
            item.setSalesDocumentId(documentId);
            item.setTenantId(tenantId);
            item.setLineNo(index + 1);
            item.setSortOrder(bo.getSortOrder() == null ? (index + 1) * 10 : bo.getSortOrder());
            mapper.insert(item);
        }
        BigDecimal listTotal = money(list), productTotal = money(product), shippingTotal = money(shipping);
        return new SalesTotals(currency, listTotal, money(listTotal.subtract(productTotal)), productTotal,
            shippingTotal, money(productTotal.add(shippingTotal)), passed);
    }

    List<SalesDocumentItemBo> current(Long documentId, Long tenantId) {
        return mapper.selectList(this.<SalesDocumentItem>active().eq("tenant_id", tenantId).eq("sales_document_id", documentId)
            .orderByAsc("line_no", "sort_order")).stream().map(this::toBo).toList();
    }

    private SalesDocumentItem failed(SalesDocumentItemBo bo, String message) {
        SalesDocumentItem item = new SalesDocumentItem();
        item.setRoomLocation(bo == null ? null : bo.getRoomLocation());
        item.setSaleProductId(bo == null ? null : bo.getSaleProductId());
        item.setOrderWidthInch(bo == null ? null : bo.getOrderWidthInch());
        item.setOrderHeightInch(bo == null ? null : bo.getOrderHeightInch());
        item.setQuantity(bo == null ? 1 : bo.getQuantity());
        item.setSelectedOptionsJson(json.write(bo == null ? null : bo.getSelectedOptionValues()));
        item.setCalculationStatus("FAIL");
        item.setCalculationMessage(message);
        item.setDelFlag("0");
        return item;
    }

    private SalesDocumentItemBo toBo(SalesDocumentItem item) {
        SalesDocumentItemBo bo = new SalesDocumentItemBo();
        bo.setRoomLocation(item.getRoomLocation());
        bo.setSaleProductId(item.getSaleProductId());
        bo.setOrderWidthInch(item.getOrderWidthInch());
        bo.setOrderHeightInch(item.getOrderHeightInch());
        bo.setQuantity(item.getQuantity());
        bo.setSelectedOptionValues(json.selections(item.getSelectedOptionsJson()));
        bo.setSortOrder(item.getSortOrder());
        bo.setRemark(item.getRemark());
        return bo;
    }

    private BigDecimal money(BigDecimal value) { return value.setScale(2, RoundingMode.HALF_UP); }
    record SalesTotals(String currency, BigDecimal listAmount, BigDecimal discountAmount,
                       BigDecimal productAmount, BigDecimal shippingAmount, BigDecimal totalAmount,
                       boolean allPassed) {}
}
