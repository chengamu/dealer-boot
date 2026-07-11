package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.uuid.Seq;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.mapper.CustomerProfileMapper;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;

@Component
@RequiredArgsConstructor
class CustomerQuoteHeaderNormalizer extends MerchantServiceSupport {

    private final CustomerProfileMapper customerMapper;
    private final SysUserMapper userMapper;

    CustomerQuote newQuote(CustomerQuoteBo bo) {
        CustomerQuote quote = baseQuote(bo);
        quote.setQuoteNo("QT-" + Seq.getId());
        quote.setStatus("DRAFT");
        quote.setProductAmount(moneyZero());
        quote.setShippingAmount(moneyZero());
        quote.setDiscountAmount(moneyZero());
        quote.setTotalAmount(moneyZero());
        return quote;
    }

    CustomerQuote updateQuote(CustomerQuote current, CustomerQuoteBo bo) {
        CustomerQuote quote = baseQuote(bo);
        quote.setQuoteId(current.getQuoteId());
        quote.setQuoteNo(current.getQuoteNo());
        quote.setStatus(current.getStatus());
        quote.setCurrencyCode(current.getCurrencyCode());
        quote.setProductAmount(current.getProductAmount());
        quote.setShippingAmount(current.getShippingAmount());
        quote.setDiscountAmount(current.getDiscountAmount());
        quote.setTotalAmount(current.getTotalAmount());
        return quote;
    }

    private CustomerQuote baseQuote(CustomerQuoteBo bo) {
        Long tenantId = currentTenantId();
        CustomerProfile customer = customerMapper.selectOne(this.<CustomerProfile>activeQuery()
            .eq("tenant_id", tenantId).eq("customer_id", bo.getCustomerId()).eq("status", STATUS_ENABLED), false);
        if (customer == null) {
            throw ServiceException.ofMessageKey("customer.quote.customer.invalid");
        }
        CustomerQuote quote = new CustomerQuote();
        quote.setTenantId(tenantId);
        quote.setCustomerId(customer.getCustomerId());
        quote.setCustomerName(customer.getCustomerName());
        quote.setCompanyName(customer.getCompanyName());
        quote.setCustomerEmail(customer.getEmail());
        quote.setProjectName(StringUtils.trim(bo.getProjectName()));
        quote.setQuoteLanguage(normalizeLanguage(bo.getQuoteLanguage()));
        quote.setValidUntil(bo.getValidUntil() == null ? LocalDate.now().plusDays(14) : bo.getValidUntil());
        resolveOwner(quote, bo.getOwnerUserId(), tenantId);
        quote.setDelFlag(DEL_FLAG_NORMAL);
        quote.setRemark(bo.getRemark());
        return quote;
    }

    private void resolveOwner(CustomerQuote quote, Long ownerUserId, Long tenantId) {
        Long id = ownerUserId == null ? LoginHelper.getUserId() : ownerUserId;
        SysUser owner = userMapper.selectOne(new QueryWrapper<SysUser>()
            .eq("tenant_id", tenantId).eq("user_id", id).eq("del_flag", DEL_FLAG_NORMAL), false);
        if (owner == null) {
            throw ServiceException.ofMessageKey("customer.quote.owner.invalid");
        }
        quote.setOwnerUserId(owner.getUserId());
        quote.setOwnerName(owner.getNickName());
    }

    private String normalizeLanguage(String language) {
        String value = StringUtils.defaultIfBlank(language, "EN_US").toUpperCase(Locale.ROOT);
        if (!"ZH_CN".equals(value) && !"EN_US".equals(value)) {
            throw ServiceException.ofMessageKey("customer.quote.language.invalid");
        }
        return value;
    }

    private BigDecimal moneyZero() {
        return BigDecimal.ZERO.setScale(2);
    }
}
