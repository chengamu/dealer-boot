package com.bocoo.dealer.payment;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.mapper.MerchantProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
@RequiredArgsConstructor
class MerchantCreditTermsReader {
    private final MerchantProfileMapper mapper;

    MerchantCreditTerms read(Long tenantId) {
        Map<String, Object> row = TenantContextHolder.callWithIgnore(() -> mapper.selectMaps(
            new QueryWrapper<MerchantProfile>().select("merchant_id", "merchant_name", "credit_limit", "credit_term_days")
                .eq("tenant_id", tenantId)).stream().findFirst().orElse(null));
        if (row == null || row.get("credit_term_days") == null || row.get("credit_limit") == null) {
            throw new ServiceException("Merchant credit limit or credit term is not configured");
        }
        return new MerchantCreditTerms(number(row.get("merchant_id")).longValue(),
            String.valueOf(row.get("merchant_name")), decimal(row.get("credit_limit")),
            number(row.get("credit_term_days")).intValue());
    }

    private Number number(Object value) {
        if (value instanceof Number number) return number;
        throw new ServiceException("Merchant credit configuration is invalid");
    }

    private BigDecimal decimal(Object value) {
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }
}
