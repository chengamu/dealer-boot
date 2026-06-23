package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductComponentBo;
import com.bocoo.product.domain.entity.ProductComponent;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.mapper.ProductComponentMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.impl.ProductComponentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductComponentServiceTest {

    @Mock
    private ProductComponentMapper componentMapper;
    @Mock
    private ProductComponentItemMapper componentItemMapper;
    @Mock
    private ProductMediaBindingMapper mediaBindingMapper;

    private ProductComponentServiceImpl productComponentService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        productComponentService = new ProductComponentServiceImpl(componentMapper, componentItemMapper, mediaBindingMapper);
    }

    @Test
    void insertComponentRejectsDuplicateCode() {
        ProductComponentBo bo = new ProductComponentBo();
        bo.setComponentCode("COMP_BASIC");
        when(componentMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productComponentService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteComponentRejectsReferences() {
        when(componentItemMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productComponentService.deleteWithValidByIds(new Long[]{170001L}))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void editCheckRejectsEnabledComponent() {
        ProductComponent component = new ProductComponent();
        component.setComponentId(170001L);
        component.setStatus("ENABLED");
        when(componentMapper.selectById(170001L)).thenReturn(component);

        BaseEditCheckResultVo result = productComponentService.checkEditAllowed(170001L);

        assertThat(result.getEditable()).isFalse();
        assertThat(result.getReasonKey()).isEqualTo("product.base.edit.enabledDenied");
    }

    @Test
    void enabledComponentCanBeDisabled() {
        when(componentMapper.update(any(), any())).thenReturn(1);

        assertThat(productComponentService.updateStatus(170001L, "DISABLED")).isTrue();
        verify(componentMapper).update(any(), any());
    }

    @Test
    void disabledComponentCanOpenEditor() {
        ProductComponent component = new ProductComponent();
        component.setComponentId(170001L);
        component.setStatus("DISABLED");
        when(componentMapper.selectById(170001L)).thenReturn(component);

        BaseEditCheckResultVo result = productComponentService.checkEditAllowed(170001L);

        assertThat(result.getEditable()).isTrue();
    }
}
