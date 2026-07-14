package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.entity.SalesDocument;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import com.bocoo.dealer.mapper.SalesDocumentQueryMapper;
import com.bocoo.dealer.service.TestSaTokenContext;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SalesDocumentBusinessQueryScopeTest {
    @Test
    void internalDetailAlwaysIncludesPlatformTenantAndOrigin() {
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 8L, "internal");
        SalesDocumentQueryMapper mapper = mock(SalesDocumentQueryMapper.class);
        SalesDocumentQueryServiceImpl service = new SalesDocumentQueryServiceImpl(mapper,
            mock(SalesDocumentEventMapper.class), mock(SalesDocumentAssembler.class),
            mock(SalesDocumentMetricsLoader.class));

        assertThatThrownBy(() -> service.queryById(9L)).isInstanceOf(ServiceException.class);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<SalesDocument>> query = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectVoOne(query.capture(), eq(false));
        assertThat(query.getValue().getSqlSegment()).contains("tenant_id", "business_origin");
        assertThat(query.getValue().getParamNameValuePairs().values())
            .contains(1L, "INTERNAL", 9L);
    }
}
