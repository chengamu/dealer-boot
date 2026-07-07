package com.bocoo.merchant.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import com.bocoo.merchant.mapper.MerchantLevelMapper;
import com.bocoo.merchant.service.impl.MerchantProfileLevelSupportImpl;
import com.bocoo.system.domain.vo.MerchantProfileLevelSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantProfileLevelSupportImplTest {

    @Mock
    private MerchantLevelMapper levelMapper;

    private MerchantProfileLevelSupportImpl service;

    @BeforeEach
    void setUp() {
        MerchantServiceTestSupport.prepare();
        service = new MerchantProfileLevelSupportImpl(levelMapper);
    }

    @Test
    void selectDefaultLevelReadsPlatformTenantLevels() {
        MerchantLevel level = level();
        when(levelMapper.selectOne(org.mockito.ArgumentMatchers.any(), eq(false))).thenReturn(level);

        MerchantProfileLevelSnapshot snapshot = service.selectDefaultLevel();

        assertThat(snapshot.levelCode()).isEqualTo("VIP");
        ArgumentCaptor<QueryWrapper<MerchantLevel>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(levelMapper).selectOne(captor.capture(), eq(false));
        assertThat(captor.getValue().getSqlSegment()).contains("tenant_id");
    }

    private MerchantLevel level() {
        MerchantLevel level = new MerchantLevel();
        level.setLevelId(1L);
        level.setLevelCode("VIP");
        level.setLevelName("VIP");
        level.setDefaultDiscountRate(new BigDecimal("0.95"));
        level.setDefaultCreditLimit(new BigDecimal("1000.00"));
        level.setStatus("ENABLED");
        level.setDelFlag("0");
        return level;
    }
}
