package com.bocoo.dealer.quickorder.runtime;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.merchant.domain.entity.MerchantLevelDiscount;
import com.bocoo.merchant.mapper.MerchantLevelDiscountMapper;
import com.bocoo.system.domain.vo.MerchantProfileVo;
import com.bocoo.system.service.MerchantProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class QuickOrderDiscountResolver {
    private final MerchantProfileService profileService;
    private final MerchantLevelDiscountMapper mapper;

    QuickOrderPricingSession session(Long tenantId) {
        MerchantProfileVo profile = profileService.selectByTenantId(tenantId);
        BigDecimal fallback = profile == null || profile.getDiscountRate() == null
            ? BigDecimal.ONE : profile.getDiscountRate();
        if (profile == null || profile.getLevelId() == null) {
            return new QuickOrderPricingSession(profile, fallback, Map.of());
        }
        List<MerchantLevelDiscount> rules = TenantContextHolder.callWithTenant(1L,
            () -> mapper.selectList(new QueryWrapper<MerchantLevelDiscount>().eq("del_flag", "0")
                .eq("status", "ENABLED").eq("level_id", profile.getLevelId())));
        Map<String, BigDecimal> rates = new LinkedHashMap<>();
        rules.forEach(row -> rates.put(key(row.getCategoryId(), row.getProductTypeCode()),
            row.getDiscountRate() == null ? fallback : row.getDiscountRate()));
        return new QuickOrderPricingSession(profile, fallback, rates);
    }

    BigDecimal resolve(QuickOrderPricingSession session, Long categoryId, String productTypeCode) {
        return session.rates().getOrDefault(key(categoryId, productTypeCode), session.fallbackRate());
    }

    private String key(Long categoryId, String productTypeCode) {
        return categoryId + "|" + (productTypeCode == null ? "" : productTypeCode);
    }
}
