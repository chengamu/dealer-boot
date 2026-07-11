package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.core.uuid.Seq;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.mapper.CustomerProfileMapper;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import com.bocoo.system.service.MerchantProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
class SalesDocumentHeaderFactory extends DealerServiceSupport {
    private final CustomerProfileMapper customerMapper;
    private final MerchantProfileService profileService;

    SalesDocument create(SalesDocumentBo bo) {
        SalesDocument row = fill(new SalesDocument(), bo);
        row.setQuoteNo("QT-" + Seq.getId());
        row.setDocumentStatus("DRAFT");
        row.setPaymentStatus("UNPAID");
        row.setProductionStatus("PENDING");
        row.setShipmentStatus("UNSHIPPED");
        row.setCurrencyCode("USD");
        row.setListAmount(zero()); row.setDiscountAmount(zero()); row.setProductAmount(zero());
        row.setShippingAmount(zero()); row.setTaxAmount(zero()); row.setTotalAmount(zero());
        return row;
    }

    SalesDocument update(SalesDocument current, SalesDocumentBo bo) {
        SalesDocument row = fill(current, bo);
        row.setSalesDocumentId(current.getSalesDocumentId());
        return row;
    }

    private SalesDocument fill(SalesDocument row, SalesDocumentBo bo) {
        Long tenantId = tenantId();
        CustomerProfile customer = customerMapper.selectOne(this.<CustomerProfile>active().eq("tenant_id", tenantId)
            .eq("customer_id", bo.getCustomerId()).eq("status", "ENABLED"), false);
        if (customer == null) throw ServiceException.ofMessageKey("dealer.sales.customerInvalid");
        MerchantProfileVo merchant = profileService.selectByTenantId(tenantId);
        row.setTenantId(tenantId);
        row.setMerchantId(merchant == null ? LoginHelper.getMerchantId() : merchant.getMerchantId());
        row.setMerchantName(merchant == null ? null : merchant.getMerchantName());
        row.setCustomerId(customer.getCustomerId()); row.setCustomerName(customer.getCustomerName());
        row.setCompanyName(customer.getCompanyName()); row.setCustomerEmail(customer.getEmail());
        row.setCustomerPhone(customer.getPhone()); row.setOwnerUserId(customer.getOwnerUserId());
        row.setOwnerName(customer.getOwnerName()); row.setProjectName(StringUtils.trim(bo.getProjectName()));
        row.setCustomerPoNo(StringUtils.trim(bo.getCustomerPoNo()));
        row.setValidUntil(bo.getValidUntil() == null ? TimeUtils.utcNow().toLocalDate().plusDays(14) : bo.getValidUntil());
        row.setRecipientName(StringUtils.blankToDefault(bo.getRecipientName(), customer.getCustomerName()));
        row.setRecipientPhone(StringUtils.blankToDefault(bo.getRecipientPhone(), customer.getPhone()));
        row.setShippingAddress(StringUtils.blankToDefault(bo.getShippingAddress(), address(customer)));
        row.setRemark(bo.getRemark()); row.setDelFlag("0");
        return row;
    }

    private String address(CustomerProfile c) {
        return java.util.stream.Stream.of(c.getAddressLine1(), c.getAddressLine2(), c.getCity(), c.getState(), c.getPostalCode(), c.getCountry())
            .filter(StringUtils::isNotBlank).collect(java.util.stream.Collectors.joining(", "));
    }

    private BigDecimal zero() { return BigDecimal.ZERO.setScale(2); }
}
