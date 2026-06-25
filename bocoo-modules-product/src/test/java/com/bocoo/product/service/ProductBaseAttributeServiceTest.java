package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductBaseAttributeBo;
import com.bocoo.product.domain.entity.ProductBaseAttribute;
import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
import com.bocoo.product.mapper.ProductBaseAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialTypeGroupMapper;
import com.bocoo.product.service.impl.ProductBaseAttributeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductBaseAttributeServiceTest {

    @Mock
    private ProductBaseAttributeMapper baseAttributeMapper;
    @Mock
    private ProductMaterialAttributeMapper materialAttributeMapper;
    @Mock
    private ProductMaterialTypeGroupMapper materialTypeGroupMapper;

    private ProductBaseAttributeServiceImpl productBaseAttributeService;

    @BeforeEach
    void setUp() {
        productBaseAttributeService = new ProductBaseAttributeServiceImpl(baseAttributeMapper, materialAttributeMapper, materialTypeGroupMapper);
    }

    @Test
    void insertBaseAttributeRejectsMissingGroup() {
        ProductBaseAttributeBo bo = new ProductBaseAttributeBo();
        bo.setAttributeCode("THICKNESS");
        bo.setAttributeNameCn("厚度");

        assertThatThrownBy(() -> productBaseAttributeService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteBaseAttributeRejectsEnabledStatus() {
        ProductBaseAttribute current = new ProductBaseAttribute();
        current.setAttributeId(1001L);
        current.setStatus("ENABLED");
        when(baseAttributeMapper.selectById(1001L)).thenReturn(current);

        assertThatThrownBy(() -> productBaseAttributeService.deleteWithValidByIds(new Long[]{1001L}))
            .isInstanceOf(ServiceException.class);
        verify(baseAttributeMapper, never()).deleteBatchIds(any());
    }

    @Test
    void normalizeBaseAttributeClearsUnitForNonNumberValueType() throws Exception {
        ProductBaseAttributeBo bo = new ProductBaseAttributeBo();
        bo.setAttributeGroupCode("FABRIC");
        bo.setAttributeCode("COLOR");
        bo.setAttributeNameCn("颜色");
        bo.setValueType("text");
        bo.setUnitCode("M");
        ProductMaterialTypeGroup group = new ProductMaterialTypeGroup();
        group.setGroupId(1L);
        group.setGroupCode("FABRIC");
        group.setGroupNameCn("面料");
        org.mockito.Mockito.when(materialTypeGroupMapper.selectOne(org.mockito.ArgumentMatchers.any())).thenReturn(group);

        Method normalize = ProductBaseAttributeServiceImpl.class.getDeclaredMethod("normalizeBaseAttribute", ProductBaseAttributeBo.class);
        normalize.setAccessible(true);
        normalize.invoke(productBaseAttributeService, bo);

        assertThat(bo.getValueType()).isEqualTo("TEXT");
        assertThat(bo.getUnitCode()).isNull();
    }
}
