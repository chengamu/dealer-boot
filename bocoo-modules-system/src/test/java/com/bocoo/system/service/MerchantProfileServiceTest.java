package com.bocoo.system.service;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.system.domain.bo.MerchantProfileBo;
import com.bocoo.system.domain.entity.MerchantProfile;
import com.bocoo.system.mapper.MerchantProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantProfileServiceTest {

    @Mock
    private MerchantProfileMapper merchantProfileMapper;
    @Mock
    private ObjectProvider<MerchantProfileLevelSupport> levelSupportProvider;

    private MerchantProfileService merchantProfileService;

    @BeforeEach
    void setUp() {
        TestSaTokenContext.install();
        merchantProfileService = new MerchantProfileService(merchantProfileMapper, levelSupportProvider);
    }

    @Test
    void updateCurrentOnlyChangesMerchantEditableFields() {
        MerchantProfile current = new MerchantProfile();
        current.setMerchantId(1001L);
        current.setTenantId(2001L);
        current.setMerchantName("Original Merchant");
        current.setCompanyName("Original Company");
        current.setPrimaryEmail("owner@example.com");
        when(merchantProfileMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(current);
        when(merchantProfileMapper.updateById(any())).thenReturn(1);

        MerchantProfileBo bo = new MerchantProfileBo();
        bo.setMerchantName("Ignored Merchant");
        bo.setCompanyName("Ignored Company");
        bo.setPrimaryEmail("ignored@example.com");
        bo.setContactFirstName("Ada");
        bo.setContactLastName("Lovelace");
        bo.setOfficePhone("010");
        bo.setCity("London");
        bo.setRemark("updated");

        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 2001L, 9001L, "merchant");

        int result = merchantProfileService.updateCurrent(bo);

        assertThat(result).isEqualTo(1);

        ArgumentCaptor<MerchantProfile> captor = ArgumentCaptor.forClass(MerchantProfile.class);
        verify(merchantProfileMapper).updateById(captor.capture());
        MerchantProfile updated = captor.getValue();
        assertThat(updated.getMerchantName()).isEqualTo("Original Merchant");
        assertThat(updated.getCompanyName()).isEqualTo("Original Company");
        assertThat(updated.getPrimaryEmail()).isEqualTo("owner@example.com");
        assertThat(updated.getContactName()).isEqualTo("Ada Lovelace");
        assertThat(updated.getOfficePhone()).isEqualTo("010");
        assertThat(updated.getCity()).isEqualTo("London");
        assertThat(updated.getRemark()).isEqualTo("updated");
    }

    @Test
    void selectCurrentRequiresMerchantTenant() {
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 9001L, "admin");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> merchantProfileService.selectCurrent())
            .isInstanceOf(com.bocoo.common.core.exception.ServiceException.class);
    }
}
