package com.bocoo.dealer.quickorder.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.uuid.Seq;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.scope.SalesOwnershipResolver;
import com.bocoo.dealer.scope.SalesOwnership;
import com.bocoo.merchant.domain.vo.CustomerProfileVo;
import com.bocoo.merchant.service.CustomerProfileService;
import com.bocoo.common.satoken.utils.LoginHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class QuickOrderHeaderFactory {
    private final CustomerProfileService customerService;
    private final SalesOwnershipResolver ownershipResolver;

    QuickOrder create(QuickOrderBo bo) {
        QuickOrder row = new QuickOrder();
        ownershipResolver.current().applyTo(row);
        initialize(row, bo, LoginHelper.getUsername());
        return row;
    }

    QuickOrder copy(QuickOrderBo bo, QuickOrder source) {
        QuickOrder row = new QuickOrder();
        SalesOwnership.from(source).applyTo(row);
        initialize(row, bo, source.getOwnerName());
        return row;
    }

    private void initialize(QuickOrder row, QuickOrderBo bo, String ownerName) {
        row.setOwnerName(ownerName); row.setQuickOrderNo("QO-" + Seq.getId());
        row.setStatus("DRAFT"); row.setDelFlag("0"); row.setTaxAmount(BigDecimal.ZERO.setScale(2));
        fill(row, bo);
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
        row.setCustomerPhone(customer.getPhone()); row.setRecipientName(bo.getRecipientName());
        row.setRecipientPhone(bo.getRecipientPhone()); row.setShippingAddress(bo.getShippingAddress());
        row.setCustomerPoNo(bo.getCustomerPoNo()); row.setRemark(bo.getRemark());
    }
}
