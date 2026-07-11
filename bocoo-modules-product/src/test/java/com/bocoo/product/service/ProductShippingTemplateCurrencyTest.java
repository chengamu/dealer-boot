package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.entity.ProductShippingTemplate;
import com.bocoo.product.mapper.ProductShippingTemplateMapper;
import com.bocoo.product.mapper.ProductShippingTemplateRuleMapper;
import com.bocoo.product.service.impl.ProductQuoteReferenceGuard;
import com.bocoo.product.service.impl.ProductShippingTemplateRuleImporter;
import com.bocoo.product.service.impl.ProductShippingTemplateRuleValidator;
import com.bocoo.product.service.impl.ProductShippingTemplateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductShippingTemplateCurrencyTest {

    @Mock
    private ProductShippingTemplateMapper templateMapper;
    @Mock
    private ProductShippingTemplateRuleMapper ruleMapper;

    @Test
    void queryListFiltersByCurrency() {
        ProductShippingTemplateServiceImpl service = new ProductShippingTemplateServiceImpl(
            templateMapper, ruleMapper, new ProductShippingTemplateRuleValidator(),
            new ProductShippingTemplateRuleImporter(), new ProductQuoteReferenceGuard(List.of()));
        ProductShippingTemplateBo bo = new ProductShippingTemplateBo();
        bo.setCurrencyCode("USD");
        when(templateMapper.selectVoList(any())).thenReturn(List.of());

        service.queryList(bo);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<QueryWrapper<ProductShippingTemplate>> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(templateMapper).selectVoList(captor.capture());
        assertThat(captor.getValue().getSqlSegment()).contains("currency_code");
        assertThat(captor.getValue().getParamNameValuePairs()).containsValue("USD");
    }
}
