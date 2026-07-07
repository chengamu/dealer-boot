package com.bocoo.merchant.service;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.bo.MerchantLevelBo;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import com.bocoo.merchant.mapper.MerchantLevelDiscountMapper;
import com.bocoo.merchant.mapper.MerchantLevelMapper;
import com.bocoo.merchant.service.impl.MerchantLevelServiceImpl;
import com.bocoo.system.mapper.MerchantProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantLevelServiceTest {

    @Mock
    private MerchantLevelMapper levelMapper;
    @Mock
    private MerchantLevelDiscountMapper discountMapper;
    @Mock
    private MerchantProfileMapper profileMapper;

    private MerchantLevelServiceImpl service;

    @BeforeEach
    void setUp() {
        MerchantServiceTestSupport.prepare();
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 1L, "admin");
        service = new MerchantLevelServiceImpl(levelMapper, discountMapper, profileMapper);
    }

    @Test
    void insertRejectsDuplicateCode() {
        when(levelMapper.selectCount(any())).thenReturn(1L);

        MerchantLevelBo bo = new MerchantLevelBo();
        bo.setLevelCode("vip");
        bo.setLevelName("VIP");

        assertThatThrownBy(() -> service.insertByBo(bo)).isInstanceOf(ServiceException.class);
        verify(levelMapper, never()).insert(any());
    }

    @Test
    void deleteRejectsEnabledLevel() {
        MerchantLevel level = new MerchantLevel();
        level.setLevelId(10L);
        level.setStatus("ENABLED");
        level.setDelFlag("0");
        when(levelMapper.selectById(10L)).thenReturn(level);

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[]{10L})).isInstanceOf(ServiceException.class);
        verify(levelMapper, never()).deleteBatchIds(any());
    }

    @Test
    void insertDefaultLevelUnsetsOtherDefaults() {
        when(levelMapper.selectCount(any())).thenReturn(0L);
        when(levelMapper.insert(any())).thenAnswer(invocation -> {
            MerchantLevel entity = invocation.getArgument(0);
            entity.setLevelId(20L);
            return 1;
        });
        when(levelMapper.update(any(), any())).thenReturn(1);

        MerchantLevelBo bo = new MerchantLevelBo();
        bo.setLevelCode("VIP");
        bo.setLevelName("VIP");
        bo.setDefaultFlag(true);
        bo.setDefaultDiscountRate(new BigDecimal("0.95"));

        service.insertByBo(bo);

        verify(levelMapper).update(any(), any());
    }

    @Test
    void updateStatusRejectsInvalidStatus() {
        assertThatThrownBy(() -> service.updateStatus(1L, "ARCHIVED")).isInstanceOf(ServiceException.class);
        verify(levelMapper, never()).update(any(), any());
    }
}
