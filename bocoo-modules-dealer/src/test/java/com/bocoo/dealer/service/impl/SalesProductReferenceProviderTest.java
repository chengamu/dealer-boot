package com.bocoo.dealer.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SalesProductReferenceProviderTest {

    @Mock
    private SalesDocumentItemMapper mapper;

    @Test
    void shippingTemplateReferenceUsesExactColumnMatch() {
        when(mapper.selectCount(any())).thenReturn(1L);
        SalesProductReferenceProvider provider = new SalesProductReferenceProvider(mapper);

        assertThat(provider.countShippingTemplateReferences(12L)).isEqualTo(1L);

        ArgumentCaptor<QueryWrapper<SalesDocumentItem>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(mapper).selectCount(captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("shipping_template_id =").doesNotContain("LIKE");
        assertThat(captor.getValue().getParamNameValuePairs()).containsValue(12L);
    }
}
