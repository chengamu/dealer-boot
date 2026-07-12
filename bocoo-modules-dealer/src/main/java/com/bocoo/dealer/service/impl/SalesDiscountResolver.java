package com.bocoo.dealer.service.impl;

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
import com.bocoo.merchant.domain.entity.CustomerQuoteItem;

@Component
@RequiredArgsConstructor
class SalesDiscountResolver {
    private final MerchantProfileService profileService;
    private final MerchantLevelDiscountMapper discountMapper;

    MerchantProfileVo profile(Long tenantId) {
        return profileService.selectByTenantId(tenantId);
    }

    Map<Long, BigDecimal> resolveAll(MerchantProfileVo profile, List<CustomerQuoteItem> items) {
        BigDecimal fallback = profile == null || profile.getDiscountRate() == null
            ? BigDecimal.ONE : profile.getDiscountRate();
        List<MerchantLevelDiscount> rules = profile == null || profile.getLevelId() == null ? List.of()
            : TenantContextHolder.callWithTenant(1L, () -> discountMapper.selectList(
                new QueryWrapper<MerchantLevelDiscount>().eq("del_flag", "0").eq("status", "ENABLED")
                    .eq("level_id", profile.getLevelId())));
        Map<String, BigDecimal> byProduct = new LinkedHashMap<>();
        rules.forEach(rule -> byProduct.put(key(rule.getCategoryId(), rule.getProductTypeCode()),
            rule.getDiscountRate() == null ? fallback : rule.getDiscountRate()));
        Map<Long, BigDecimal> result = new LinkedHashMap<>();
        items.forEach(item -> result.put(item.getQuoteItemId(),
            byProduct.getOrDefault(key(item.getCategoryId(), item.getProductTypeCode()), fallback)));
        return result;
    }

    private String key(Long categoryId, String productTypeCode) {
        return categoryId + "|" + (productTypeCode == null ? "" : productTypeCode);
    }
}
