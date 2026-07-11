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

@Component
@RequiredArgsConstructor
class SalesDiscountResolver {
    private final MerchantProfileService profileService;
    private final MerchantLevelDiscountMapper discountMapper;

    MerchantProfileVo profile(Long tenantId) {
        return profileService.selectByTenantId(tenantId);
    }

    BigDecimal resolve(MerchantProfileVo profile, Long categoryId, String productTypeCode) {
        if (profile == null) return BigDecimal.ONE;
        MerchantLevelDiscount rule = TenantContextHolder.callWithTenant(1L, () ->
            discountMapper.selectOne(new QueryWrapper<MerchantLevelDiscount>()
                .eq("del_flag", "0").eq("status", "ENABLED")
                .eq("level_id", profile.getLevelId()).eq("category_id", categoryId)
                .eq("product_type_code", productTypeCode).last("limit 1"), false));
        BigDecimal rate = rule == null ? profile.getDiscountRate() : rule.getDiscountRate();
        return rate == null ? BigDecimal.ONE : rate;
    }
}
