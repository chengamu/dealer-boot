package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.impl.ProductCategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCategoryServiceTest {

    @Mock
    private ProductCategoryMapper categoryMapper;
    @Mock
    private ProductMediaBindingMapper mediaBindingMapper;

    private ProductCategoryServiceImpl productCategoryService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        productCategoryService = new ProductCategoryServiceImpl(categoryMapper, mediaBindingMapper);
    }

    @Test
    void insertCategoryRejectsDuplicateCode() {
        ProductCategoryBo bo = new ProductCategoryBo();
        bo.setCategoryCode("ROLLER_SHADE");
        bo.setCategoryNameCn("卷帘");
        when(categoryMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productCategoryService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteCategoryRejectsReferences() {
        ProductCategory category = new ProductCategory();
        category.setCategoryId(1001L);
        category.setCategoryCode("ROLLER_SHADE");
        category.setStatus("DISABLED");
        when(categoryMapper.selectById(1001L)).thenReturn(category);
        when(categoryMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productCategoryService.deleteWithValidByIds(new Long[]{1001L}))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteCategoryRejectsEnabledStatus() {
        ProductCategory category = new ProductCategory();
        category.setCategoryId(1001L);
        category.setStatus("ENABLED");
        when(categoryMapper.selectById(1001L)).thenReturn(category);

        assertThatThrownBy(() -> productCategoryService.deleteWithValidByIds(new Long[]{1001L}))
            .isInstanceOf(ServiceException.class);
        verify(categoryMapper, never()).deleteBatchIds(any());
    }

    @Test
    void editCheckRejectsEnabledCategory() {
        ProductCategory category = new ProductCategory();
        category.setCategoryId(1001L);
        category.setStatus("ENABLED");
        when(categoryMapper.selectById(1001L)).thenReturn(category);

        BaseEditCheckResultVo result = productCategoryService.checkEditAllowed(1001L);

        assertThat(result.getEditable()).isFalse();
        assertThat(result.getReasonKey()).isEqualTo("product.base.edit.enabledDenied");
    }

    @Test
    void updateCategoryRejectsDescendantAsParent() {
        ProductCategory child = new ProductCategory();
        child.setCategoryId(2001L);
        child.setParentId(1001L);
        child.setCategoryCode("ROLLER_SHADE_BASIC");
        child.setCategoryLevel(2);
        child.setCategoryPath("WINDOW_COVERING/ROLLER_SHADE_BASIC");
        child.setDelFlag("0");
        when(categoryMapper.selectById(2001L)).thenReturn(child);

        ProductCategoryBo bo = new ProductCategoryBo();
        bo.setCategoryId(1001L);
        bo.setParentId(2001L);
        bo.setCategoryCode("WINDOW_COVERING");
        bo.setCategoryNameCn("窗饰产品");

        assertThatThrownBy(() -> productCategoryService.updateByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void enabledCategoryCanBeDisabled() {
        when(categoryMapper.update(any(), any())).thenReturn(1);

        assertThat(productCategoryService.updateStatus(1001L, "DISABLED")).isTrue();
        verify(categoryMapper).update(any(), any());
    }

    @Test
    void disabledCategoryCanOpenEditor() {
        ProductCategory category = new ProductCategory();
        category.setCategoryId(1001L);
        category.setStatus("DISABLED");
        when(categoryMapper.selectById(1001L)).thenReturn(category);

        BaseEditCheckResultVo result = productCategoryService.checkEditAllowed(1001L);

        assertThat(result.getEditable()).isTrue();
    }
}
