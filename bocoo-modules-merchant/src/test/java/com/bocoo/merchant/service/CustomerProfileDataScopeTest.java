package com.bocoo.merchant.service;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.mapper.CustomerProfileMapper;
import com.bocoo.merchant.mapper.CustomerQuoteMapper;
import com.bocoo.merchant.service.impl.CustomerProfileNormalizer;
import com.bocoo.merchant.service.impl.CustomerProfileServiceImpl;
import com.bocoo.merchant.service.impl.SalesOwnershipResolver;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.MerchantProfileMapper;
import com.bocoo.system.mapper.SysUserMapper;
import com.bocoo.system.service.SalesStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CustomerProfileDataScopeTest {

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
    void queryByIdUsesDataPermissionScope() {
        when(customerMapper.selectOne(any())).thenReturn(disabledCustomer());

        service.queryById(1L);

        verify(customerMapper).selectOne(any());
        verify(customerMapper, never()).selectOne(any(), eq(false));
    }

    @Test
    void updateUsesDataPermissionScope() {
        when(customerMapper.selectOne(any())).thenReturn(disabledCustomer());
        when(customerMapper.selectCount(any())).thenReturn(0L);
        when(customerMapper.updateById(any())).thenReturn(1);

        assertThat(service.updateByBo(updateBo())).isTrue();

        verify(customerMapper).selectOne(any());
        verify(customerMapper, never()).selectOne(any(), eq(false));
    }

    @Test
    void deleteUsesDataPermissionScope() {
        when(customerMapper.selectOne(any())).thenReturn(disabledCustomer());
        when(quoteMapper.selectCount(any())).thenReturn(0L);
        when(customerMapper.delete(any())).thenReturn(1);

        assertThat(service.deleteWithValidByIds(new Long[]{1L})).isTrue();

        verify(customerMapper).selectOne(any());
        verify(customerMapper, never()).selectOne(any(), eq(false));
    }

    @Test
    void updateStatusUsesDataPermissionScope() {
        when(customerMapper.selectOne(any())).thenReturn(disabledCustomer());

        service.updateStatus(1L, "ENABLED");

        verify(customerMapper).selectOne(any());
    }

    private CustomerProfile disabledCustomer() {
        CustomerProfile customer = new CustomerProfile();
        customer.setCustomerId(1L);
        customer.setTenantId(200L);
        customer.setBusinessOrigin("MERCHANT");
        customer.setStatus("DISABLED");
        return customer;
    }

    private CustomerProfileBo updateBo() {
        CustomerProfileBo bo = new CustomerProfileBo();
        bo.setCustomerId(1L);
        bo.setCustomerName("Scoped Customer");
        bo.setEmail("scoped@example.com");
        return bo;
    }
}
