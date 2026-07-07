package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import com.bocoo.merchant.mapper.MerchantLevelMapper;
import com.bocoo.system.domain.vo.MerchantProfileLevelSnapshot;
import com.bocoo.system.service.MerchantProfileLevelSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantProfileLevelSupportImpl extends MerchantServiceSupport implements MerchantProfileLevelSupport {

    private final MerchantLevelMapper levelMapper;

    @Override
    public MerchantProfileLevelSnapshot selectDefaultLevel() {
        return platformIgnoreTenant(() -> {
            MerchantLevel level = levelMapper.selectOne(this.<MerchantLevel>activeQuery()
                .eq("tenant_id", PLATFORM_TENANT_ID)
                .eq("status", STATUS_ENABLED)
                .eq("default_flag", true)
                .orderByAsc("sort_order", "level_id")
                .last("limit 1"), false);
            if (level == null) {
                level = levelMapper.selectOne(this.<MerchantLevel>activeQuery()
                    .eq("tenant_id", PLATFORM_TENANT_ID)
                    .eq("status", STATUS_ENABLED)
                    .orderByAsc("sort_order", "level_id")
                    .last("limit 1"), false);
            }
            return level == null ? null : toSnapshot(level);
        });
    }

    @Override
    public MerchantProfileLevelSnapshot selectEnabledLevel(Long levelId) {
        MerchantLevel level = platformIgnoreTenant(() -> levelMapper.selectOne(this.<MerchantLevel>activeQuery()
            .eq("tenant_id", PLATFORM_TENANT_ID)
            .eq("level_id", levelId)
            .eq("status", STATUS_ENABLED), false));
        if (level == null) {
            throw ServiceException.ofMessageKey("merchant.level.notFound");
        }
        return toSnapshot(level);
    }

    private MerchantProfileLevelSnapshot toSnapshot(MerchantLevel level) {
        return new MerchantProfileLevelSnapshot(level.getLevelId(), level.getLevelCode(), level.getLevelName(),
            level.getDefaultDiscountRate(), level.getDefaultCreditLimit());
    }
}
