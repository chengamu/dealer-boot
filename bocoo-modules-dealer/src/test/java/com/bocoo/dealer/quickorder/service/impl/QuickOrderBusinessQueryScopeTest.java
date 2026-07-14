package com.bocoo.dealer.quickorder.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.quickorder.domain.entity.QuickOrder;
import com.bocoo.dealer.quickorder.mapper.QuickOrderQueryMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class QuickOrderBusinessQueryScopeTest {
    @Test
    void merchantDetailAlwaysIncludesCurrentTenantAndOrigin() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.MERCHANT.getCode(), 300001L, 7L, "seller");
        QuickOrderQueryMapper mapper = mock(QuickOrderQueryMapper.class);
        QuickOrderQueryServiceImpl service = new QuickOrderQueryServiceImpl(
            mapper, mock(QuickOrderAssembler.class));

        assertThatThrownBy(() -> service.queryById(9L)).isInstanceOf(ServiceException.class);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<QuickOrder>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectVoOne(query.capture(), eq(false));
        assertThat(query.getValue().getSqlSegment()).contains("tenant_id", "business_origin");
        assertThat(query.getValue().getParamNameValuePairs().values())
            .contains(300001L, "MERCHANT", 9L);
    }
}
