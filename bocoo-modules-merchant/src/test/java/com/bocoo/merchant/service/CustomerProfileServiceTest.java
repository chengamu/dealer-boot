package com.bocoo.merchant.service;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.domain.vo.CustomerOwnerOptionVo;
import com.bocoo.merchant.mapper.CustomerProfileMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.impl.CustomerProfileServiceImpl;
import com.bocoo.merchant.service.impl.CustomerProfileNormalizer;
import com.bocoo.merchant.service.impl.SalesOwnershipResolver;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.mapper.MerchantProfileMapper;
import com.bocoo.system.mapper.SysUserMapper;
import com.bocoo.system.service.SalesStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

    @Mock
    private CustomerProfileMapper customerMapper;
    @Mock
    private MerchantProfileMapper merchantProfileMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private CustomerQuoteMapper quoteMapper;
    @Mock
    private SalesStoreService salesStoreService;

    private CustomerProfileServiceImpl service;

    @BeforeEach
    void setUp() {
        MerchantServiceTestSupport.prepare();
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 200L, 10L, "merchant");
        SysUser currentOwner = new SysUser();
        currentOwner.setUserId(10L);
        currentOwner.setNickName("Sales");
        lenient().when(userMapper.selectOne(any(), eq(false))).thenReturn(currentOwner);
        SalesOwnershipResolver ownershipResolver = new SalesOwnershipResolver(salesStoreService);
        CustomerProfileNormalizer normalizer = new CustomerProfileNormalizer(
            merchantProfileMapper, userMapper, ownershipResolver);
        service = new CustomerProfileServiceImpl(customerMapper, userMapper, quoteMapper,
            org.mockito.Mockito.mock(org.springframework.beans.factory.ObjectProvider.class), normalizer, ownershipResolver);
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
    void deleteRejectsCustomerReferencedByQuote() {
        CustomerProfile customer = new CustomerProfile();
        customer.setCustomerId(1L);
        customer.setTenantId(200L);
        customer.setStatus("DISABLED");
        customer.setDelFlag("0");
        when(customerMapper.selectOne(any(), eq(false))).thenReturn(customer);
        when(quoteMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[]{1L}))
            .isInstanceOf(ServiceException.class);

        verify(customerMapper, never()).delete(any());
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

        ArgumentCaptor<CustomerProfile> captor = ArgumentCaptor.forClass(CustomerProfile.class);
        verify(customerMapper).insert(captor.capture());
        assertThat(captor.getValue().getTenantId()).isEqualTo(200L);
        assertThat(captor.getValue().getBusinessOrigin()).isEqualTo("MERCHANT");
        assertThat(captor.getValue().getSalesStoreId()).isNull();
        assertThat(captor.getValue().getDeptId()).isEqualTo(100L);
        assertThat(captor.getValue().getOwnerUserId()).isEqualTo(10L);
    }

    @Test
    void updateAllowsExistingCustomerWithoutOwner() {
        CustomerProfile current = new CustomerProfile();
        current.setCustomerId(1L);
        current.setTenantId(200L);
        current.setBusinessOrigin("MERCHANT");
        current.setDeptId(100L);
        current.setOwnerUserId(null);
        current.setOwnerName(null);
        when(customerMapper.selectOne(any(), eq(false))).thenReturn(current);
        when(customerMapper.selectCount(any())).thenReturn(0L);
        when(customerMapper.updateById(any())).thenReturn(1);

        CustomerProfileBo bo = new CustomerProfileBo();
        bo.setCustomerId(1L);
        bo.setCustomerName("Unassigned Customer");
        bo.setEmail("unassigned@example.com");

        assertThat(service.updateByBo(bo)).isTrue();

        verify(userMapper, never()).selectOne(any(), eq(false));
    }

    @Test
    void insertInternalCustomerUsesEnabledDepartmentStore() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 1L, "internal", 21L);
        SalesStoreVo store = new SalesStoreVo();
        store.setSalesStoreId(501L);
        when(salesStoreService.resolveEnabledByDeptId(21L)).thenReturn(store);
        when(customerMapper.selectCount(any())).thenReturn(0L);
        when(customerMapper.insert(any())).thenReturn(1);

        CustomerProfileBo bo = new CustomerProfileBo();
        bo.setCustomerName("Internal Customer");
        bo.setEmail("internal@example.com");

        service.insertByBo(bo);

        ArgumentCaptor<CustomerProfile> captor = ArgumentCaptor.forClass(CustomerProfile.class);
        verify(customerMapper).insert(captor.capture());
        CustomerProfile saved = captor.getValue();
        assertThat(saved.getTenantId()).isEqualTo(1L);
        assertThat(saved.getBusinessOrigin()).isEqualTo("INTERNAL");
        assertThat(saved.getSalesStoreId()).isEqualTo(501L);
        assertThat(saved.getDeptId()).isEqualTo(21L);
        assertThat(saved.getOwnerUserId()).isEqualTo(1L);
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
