package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.uuid.Seq;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.merchant.domain.vo.CustomerProfileVo;
import com.bocoo.merchant.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class QuickOrderHeaderFactory {
    private final CustomerProfileService customerService;

    QuickOrder create(QuickOrderBo bo, Long tenantId) {
        QuickOrder row = new QuickOrder();
        row.setTenantId(tenantId); row.setQuickOrderNo("QO-" + Seq.getId());
        row.setStatus("DRAFT"); row.setDelFlag("0"); row.setTaxAmount(BigDecimal.ZERO.setScale(2));
        fill(row, bo); return row;
    }

    void update(QuickOrder row, QuickOrderBo bo) {
        fill(row, bo);
    }

    private void fill(QuickOrder row, QuickOrderBo bo) {
        if (bo.getCustomerId() == null) throw ServiceException.ofMessageKey("dealer.quickOrder.customer.required");
        CustomerProfileVo customer = customerService.queryById(bo.getCustomerId());
        if (customer == null || !"ENABLED".equalsIgnoreCase(customer.getStatus())) {
            throw ServiceException.ofMessageKey("dealer.quickOrder.customer.invalid");
        }
        row.setCustomerId(customer.getCustomerId()); row.setCustomerName(customer.getCustomerName());
        row.setCompanyName(customer.getCompanyName()); row.setCustomerEmail(customer.getEmail());
        row.setCustomerPhone(customer.getPhone()); row.setOwnerUserId(customer.getOwnerUserId());
        row.setOwnerName(customer.getOwnerName()); row.setRecipientName(bo.getRecipientName());
        row.setRecipientPhone(bo.getRecipientPhone()); row.setShippingAddress(bo.getShippingAddress());
        row.setCustomerPoNo(bo.getCustomerPoNo()); row.setRemark(bo.getRemark());
    }
}
