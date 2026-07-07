package com.bocoo.system.service;

import com.bocoo.system.domain.vo.MerchantProfileLevelSnapshot;

public interface MerchantProfileLevelSupport {

    MerchantProfileLevelSnapshot selectDefaultLevel();

    MerchantProfileLevelSnapshot selectEnabledLevel(Long levelId);
}
