package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductComponentItemBo;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.service.impl.ProductComponentItemServiceImpl;
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
class ProductComponentItemServiceTest {

    @Mock
    private ProductComponentItemMapper componentItemMapper;

    private ProductComponentItemServiceImpl productComponentItemService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        productComponentItemService = new ProductComponentItemServiceImpl(componentItemMapper);
    }

    @Test
    void insertComponentItemRejectsDuplicateMaterialInComponent() {
        ProductComponentItemBo bo = new ProductComponentItemBo();
        bo.setComponentId(170001L);
        bo.setMaterialId(130001L);
        when(componentItemMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productComponentItemService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void insertComponentItemRejectsDuplicateSortInComponent() {
        ProductComponentItemBo bo = new ProductComponentItemBo();
        bo.setComponentId(170001L);
        bo.setSortOrder(10);
        when(componentItemMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productComponentItemService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteComponentItemWithoutReferencesUsesMapperRemove() {
        when(componentItemMapper.deleteBatchIds(any())).thenReturn(1);

        assertThat(productComponentItemService.deleteWithValidByIds(new Long[]{180001L})).isTrue();
        verify(componentItemMapper).deleteBatchIds(any());
    }

    @Test
    void editCheckRejectsEnabledComponentItem() {
        ProductComponentItem item = new ProductComponentItem();
        item.setComponentItemId(180001L);
        item.setStatus("ENABLED");
        when(componentItemMapper.selectById(180001L)).thenReturn(item);

        BaseEditCheckResultVo result = productComponentItemService.checkEditAllowed(180001L);

        assertThat(result.getEditable()).isFalse();
        assertThat(result.getReasonKey()).isEqualTo("product.base.edit.enabledDenied");
    }

    @Test
    void enabledComponentItemCanBeDisabled() {
        when(componentItemMapper.update(any(), any())).thenReturn(1);

        assertThat(productComponentItemService.updateStatus(180001L, "DISABLED")).isTrue();
        verify(componentItemMapper).update(any(), any());
    }

    @Test
    void disabledComponentItemCanOpenEditor() {
        ProductComponentItem item = new ProductComponentItem();
        item.setComponentItemId(180001L);
        item.setStatus("DISABLED");
        when(componentItemMapper.selectById(180001L)).thenReturn(item);

        BaseEditCheckResultVo result = productComponentItemService.checkEditAllowed(180001L);

        assertThat(result.getEditable()).isTrue();
    }
}
