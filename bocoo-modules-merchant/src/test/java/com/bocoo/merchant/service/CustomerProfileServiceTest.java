package com.bocoo.merchant.service;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.domain.vo.CustomerOwnerOptionVo;
import com.bocoo.merchant.mapper.CustomerProfileMapper;
import com.bocoo.merchant.service.impl.CustomerProfileServiceImpl;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.MerchantProfileMapper;
import com.bocoo.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

    @Mock
    private CustomerProfileMapper customerMapper;
    @Mock
    private MerchantProfileMapper merchantProfileMapper;
    @Mock
    private SysUserMapper userMapper;

    private CustomerProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        MerchantServiceTestSupport.prepare();
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 200L, 10L, "merchant");
        service = new CustomerProfileServiceImpl(customerMapper, merchantProfileMapper, userMapper);
    }

    @Test
    void insertRejectsDuplicateEmailInTenant() {
        when(customerMapper.selectCount(any())).thenReturn(1L);

        CustomerProfileBo bo = new CustomerProfileBo();
        bo.setCustomerName("ABC Customer");
        bo.setEmail("Owner@Example.com");

        assertThatThrownBy(() -> service.insertByBo(bo)).isInstanceOf(ServiceException.class);
        verify(customerMapper, never()).insert(any());
    }

    @Test
    void insertRejectsOwnerFromOtherTenant() {
        when(userMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(null);

        CustomerProfileBo bo = new CustomerProfileBo();
        bo.setCustomerName("ABC Customer");
        bo.setEmail("owner@example.com");
        bo.setOwnerUserId(99L);

        assertThatThrownBy(() -> service.insertByBo(bo)).isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteRejectsEnabledCustomer() {
        CustomerProfile customer = new CustomerProfile();
        customer.setCustomerId(1L);
        customer.setTenantId(200L);
        customer.setStatus("ENABLED");
        customer.setDelFlag("0");
        when(customerMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(customer);

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[]{1L})).isInstanceOf(ServiceException.class);
        verify(customerMapper, never()).deleteBatchIds(any());
    }

    @Test
    void deleteRejectsCustomerOutsideCurrentTenant() {
        when(customerMapper.selectOne(any(), eq(false))).thenReturn(null);

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[]{1L})).isInstanceOf(ServiceException.class);
        verify(customerMapper, never()).delete(any());
    }

    @Test
    void deleteUsesCurrentTenantCondition() {
        CustomerProfile customer = new CustomerProfile();
        customer.setCustomerId(1L);
        customer.setTenantId(200L);
        customer.setStatus("DISABLED");
        customer.setDelFlag("0");
        when(customerMapper.selectOne(any(), eq(false))).thenReturn(customer);
        when(customerMapper.delete(any())).thenReturn(1);

        service.deleteWithValidByIds(new Long[]{1L});

        verify(customerMapper).delete(any());
        verify(customerMapper, never()).deleteBatchIds(any());
    }

    @Test
    void insertAcceptsOwnerInCurrentTenant() {
        SysUser owner = new SysUser();
        owner.setUserId(10L);
        owner.setNickName("Sales");
        when(userMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(owner);
        when(customerMapper.selectCount(any())).thenReturn(0L);
        when(customerMapper.insert(any())).thenReturn(1);

        CustomerProfileBo bo = new CustomerProfileBo();
        bo.setCustomerName("ABC Customer");
        bo.setEmail("Owner@Example.com");
        bo.setOwnerUserId(10L);

        service.insertByBo(bo);

        verify(customerMapper).insert(any());
    }

    @Test
    void updateStatusRejectsInvalidStatus() {
        assertThatThrownBy(() -> service.updateStatus(1L, "ARCHIVED")).isInstanceOf(ServiceException.class);
        verify(customerMapper, never()).update(any(), any());
    }

    @Test
    void queryOwnerOptionsUsesCurrentTenantUsers() {
        SysUser owner = new SysUser();
        owner.setUserId(10L);
        owner.setUserName("sales");
        owner.setNickName("Sales");
        when(userMapper.selectList(any())).thenReturn(List.of(owner));

        List<CustomerOwnerOptionVo> options = service.queryOwnerOptions(null);

        assertThat(options).hasSize(1);
        assertThat(options.get(0).getUserId()).isEqualTo(10L);
        verify(userMapper).selectList(any());
    }

    @Test
    void queryOwnerOptionsFallsBackToCurrentTenantForPlatformProfilePage() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 1L, "admin");
        SysUser owner = new SysUser();
        owner.setUserId(1L);
        owner.setUserName("admin");
        owner.setNickName("System Admin");
        when(userMapper.selectList(any())).thenReturn(List.of(owner));

        List<CustomerOwnerOptionVo> options = service.queryOwnerOptions(null);

        assertThat(options).hasSize(1);
        assertThat(options.get(0).getUserId()).isEqualTo(1L);
        verify(userMapper).selectList(any());
    }
}
