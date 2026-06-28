package com.bocoo.system.service;

import com.bocoo.common.core.enums.TenantApplyStatus;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.system.domain.bo.SysTenantApplyBo;
import com.bocoo.system.domain.entity.SysTenantApply;
import com.bocoo.system.mapper.SysTenantApplyMapper;
import com.bocoo.system.mapper.SysTenantMapper;
import com.bocoo.system.mapper.SysUserMapper;
import com.bocoo.system.mapper.SysUserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysTenantApplyServiceTest {

    @Mock
    private SysTenantApplyMapper tenantApplyMapper;
    @Mock
    private SysTenantMapper tenantMapper;
    @Mock
    private SysUserService userService;
    @Mock
    private MerchantProfileService merchantProfileService;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SysUserRoleMapper userRoleMapper;
    @Mock
    private MerchantAccountDefaultsService merchantDefaultsService;

    private SysTenantApplyService tenantApplyService;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        tenantApplyService = new SysTenantApplyService(
            tenantApplyMapper,
            tenantMapper,
            userService,
            merchantProfileService,
            userMapper,
            userRoleMapper,
            merchantDefaultsService
        );
    }

    @Test
    void rejectOnlyAllowsPendingApplications() {
        SysTenantApply approved = new SysTenantApply();
        approved.setApplyId(1001L);
        approved.setStatus(TenantApplyStatus.APPROVED.getCode());
        when(tenantApplyMapper.selectById(anyLong())).thenReturn(approved);

        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9001L, "auditor");

        assertThatThrownBy(() -> tenantApplyService.reject(1001L, new SysTenantApplyBo()))
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }

    @Test
    void rejectMarksApplicationRejectedWithAuditInfo() {
        SysTenantApply pending = new SysTenantApply();
        pending.setApplyId(1001L);
        pending.setMerchantName("Acme");
        pending.setEmail("owner@example.com");
        pending.setStatus(TenantApplyStatus.PENDING.getCode());
        when(tenantApplyMapper.selectById(anyLong())).thenReturn(pending);

        SysTenantApplyBo bo = new SysTenantApplyBo();
        bo.setRejectReason("Missing license");

        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9001L, "auditor");

        tenantApplyService.reject(1001L, bo);

        ArgumentCaptor<SysTenantApply> captor = ArgumentCaptor.forClass(SysTenantApply.class);
        verify(tenantApplyMapper).updateById(captor.capture());
        SysTenantApply updated = captor.getValue();
        assertThat(updated.getStatus()).isEqualTo(TenantApplyStatus.REJECTED.getCode());
        assertThat(updated.getRejectReason()).isEqualTo("Missing license");
        assertThat(updated.getAuditBy()).isEqualTo("auditor");
        assertThat(updated.getAuditById()).isEqualTo(9001L);
        assertThat(updated.getAuditTime()).isNotNull();
    }
}
