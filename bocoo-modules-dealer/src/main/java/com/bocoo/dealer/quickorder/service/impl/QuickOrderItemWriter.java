package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderItemBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrderItem;
import com.bocoo.dealer.quickorder.mapper.QuickOrderItemMapper;
import com.bocoo.dealer.quickorder.runtime.QuickOrderPricingEngine;
import com.bocoo.dealer.quickorder.runtime.QuickOrderPricingSession;
import com.bocoo.dealer.quickorder.runtime.QuickOrderJson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
class QuickOrderItemWriter {
    private final QuickOrderItemMapper mapper;
    private final QuickOrderPricingEngine pricingEngine;
    private final QuickOrderJson json;

    QuickOrderItemWriteResult replace(Long orderId, Long tenantId, List<QuickOrderItemBo> rows) {
        mapper.delete(new QueryWrapper<QuickOrderItem>().eq("del_flag", "0")
            .eq("tenant_id", tenantId).eq("quick_order_id", orderId));
        if (rows == null || rows.isEmpty()) return new QuickOrderItemWriteResult(List.of(), "USD");
        QuickOrderPricingSession session = pricingEngine.openSession(tenantId);
        List<QuickOrderItem> saved = new ArrayList<>();
        String currency = null;
        for (int index = 0; index < rows.size(); index++) {
            var result = pricingEngine.calculate(rows.get(index), session);
            if (currency != null && !currency.equals(result.currencyCode())) {
                throw com.bocoo.common.core.exception.ServiceException.ofMessageKey("dealer.quickOrder.currency.mismatch");
            }
            currency = result.currencyCode();
            QuickOrderItem item = result.item();
            item.setQuickOrderItemId(null); item.setQuickOrderId(orderId); item.setTenantId(tenantId);
            item.setLineNo(index + 1);
            if (item.getSortOrder() == null) item.setSortOrder((index + 1) * 10);
            mapper.insert(item); saved.add(item);
        }
        return new QuickOrderItemWriteResult(saved, currency);
    }

    void copyPending(Long orderId, Long tenantId, List<QuickOrderItem> source) {
        for (int index = 0; index < source.size(); index++) {
            QuickOrderItem old = source.get(index);
            QuickOrderItem item = new QuickOrderItem();
            item.setQuickOrderId(orderId); item.setTenantId(tenantId); item.setLineNo(index + 1);
            item.setRoomLocation(old.getRoomLocation()); item.setSaleProductId(old.getSaleProductId());
            item.setSaleProductCode(old.getSaleProductCode()); item.setSaleProductName(old.getSaleProductName());
            item.setCategoryId(old.getCategoryId()); item.setCategoryCode(old.getCategoryCode());
            item.setCategoryNameCn(old.getCategoryNameCn()); item.setProductTypeCode(old.getProductTypeCode());
            item.setProductTypeNameCn(old.getProductTypeNameCn()); item.setFormulaId(old.getFormulaId());
            item.setFormulaVersionId(old.getFormulaVersionId()); item.setFormulaVersionLabel(old.getFormulaVersionLabel());
            item.setOrderWidthInch(old.getOrderWidthInch()); item.setOrderHeightInch(old.getOrderHeightInch());
            item.setQuantity(old.getQuantity()); item.setSelectedOptionsJson(json.write(json.readSelections(old.getSelectedOptionsJson())));
            item.setCalculationStatus("PENDING"); item.setSortOrder(old.getSortOrder());
            item.setRemark(old.getRemark()); item.setDelFlag("0"); mapper.insert(item);
        }
    }
}
