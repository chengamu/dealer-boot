package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.entity.CustomerProfile;
import com.bocoo.merchant.domain.entity.CustomerQuote;
import com.bocoo.merchant.service.CustomerQuoteConversionSnapshot;
import com.bocoo.merchant.service.TestSaTokenContext;
import com.bocoo.merchant.mapper.CustomerProfileMapper;
import com.bocoo.system.domain.entity.SysUser;
import com.bocoo.system.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerQuoteOwnershipSnapshotTest {

    @Mock
    private CustomerProfileMapper customerMapper;
    @Mock
    private SysUserMapper userMapper;
    @Mock
    private SalesOwnershipResolver ownershipResolver;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "internal-sales", 21L);
        CustomerProfile customer = new CustomerProfile();
        customer.setCustomerId(10L);
        customer.setCustomerName("Customer");
        org.mockito.Mockito.lenient().when(customerMapper.selectOne(any(), eq(false))).thenReturn(customer);
        SysUser owner = new SysUser();
        owner.setUserId(8L);
        owner.setNickName("Internal Sales");
        org.mockito.Mockito.lenient().when(userMapper.selectOne(any(), eq(false))).thenReturn(owner);
    }

    @Test
    void newQuoteFreezesInternalOwnership() {
        when(ownershipResolver.currentForCreate()).thenReturn(
            new SalesOwnership(1L, "INTERNAL", 501L, 21L, 8L));
        CustomerQuoteBo bo = quoteBo();

        CustomerQuote quote = normalizer().newQuote(bo);

        assertThat(quote.getTenantId()).isEqualTo(1L);
        assertThat(quote.getBusinessOrigin()).isEqualTo("INTERNAL");
        assertThat(quote.getSalesStoreId()).isEqualTo(501L);
        assertThat(quote.getDeptId()).isEqualTo(21L);
        assertThat(quote.getOwnerUserId()).isEqualTo(8L);
    }

    @Test
    void updateKeepsExistingOwnershipSnapshot() {
        CustomerQuote current = new CustomerQuote();
        current.setTenantId(300L);
        current.setBusinessOrigin("MERCHANT");
        current.setSalesStoreId(null);
        current.setDeptId(31L);
        current.setOwnerUserId(9L);
        SysUser owner = new SysUser();
        owner.setUserId(9L);
        owner.setNickName("Merchant Sales");
        when(userMapper.selectOne(any(), eq(false))).thenReturn(owner);

        CustomerQuote quote = normalizer().updateQuote(current, quoteBo());

        assertThat(quote.getTenantId()).isEqualTo(300L);
        assertThat(quote.getBusinessOrigin()).isEqualTo("MERCHANT");
        assertThat(quote.getSalesStoreId()).isNull();
        assertThat(quote.getDeptId()).isEqualTo(31L);
        assertThat(quote.getOwnerUserId()).isEqualTo(9L);
    }

    @Test
    void conversionSnapshotExposesFrozenOwnership() {
        CustomerQuote quote = new CustomerQuote();
        quote.setBusinessOrigin("INTERNAL");
        quote.setSalesStoreId(501L);
        quote.setDeptId(21L);
        quote.setOwnerUserId(8L);

        CustomerQuoteConversionSnapshot snapshot = new CustomerQuoteConversionSnapshot(quote, List.of());

        assertThat(snapshot.businessOrigin()).isEqualTo("INTERNAL");
        assertThat(snapshot.salesStoreId()).isEqualTo(501L);
        assertThat(snapshot.deptId()).isEqualTo(21L);
        assertThat(snapshot.ownerUserId()).isEqualTo(8L);
    }

    private CustomerQuoteHeaderNormalizer normalizer() {
        return new CustomerQuoteHeaderNormalizer(customerMapper, userMapper, ownershipResolver);
    }

    private CustomerQuoteBo quoteBo() {
        CustomerQuoteBo bo = new CustomerQuoteBo();
        bo.setCustomerId(10L);
        bo.setProjectName("Project");
        bo.setQuoteLanguage("EN_US");
        return bo;
    }
}
