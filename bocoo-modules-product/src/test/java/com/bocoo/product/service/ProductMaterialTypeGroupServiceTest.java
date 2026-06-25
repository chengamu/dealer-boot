package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductMaterialTypeGroupBo;
import com.bocoo.product.domain.entity.ProductMaterialType;
import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductBaseAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMaterialTypeGroupMapper;
import com.bocoo.product.mapper.ProductMaterialTypeMapper;
import com.bocoo.product.service.impl.ProductMaterialTypeGroupServiceImpl;
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
class ProductMaterialTypeGroupServiceTest {

    @Mock
    private ProductMaterialTypeGroupMapper groupMapper;
    @Mock
    private ProductMaterialTypeMapper typeMapper;
    @Mock
    private ProductBaseAttributeMapper baseAttributeMapper;
    @Mock
    private ProductMaterialMapper materialMapper;

    private ProductMaterialTypeGroupServiceImpl service;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        service = new ProductMaterialTypeGroupServiceImpl(groupMapper, typeMapper, baseAttributeMapper, materialMapper);
    }

    @Test
    void insertRejectsDuplicateGroupCode() {
        ProductMaterialTypeGroupBo bo = new ProductMaterialTypeGroupBo();
        bo.setGroupCode("control");
        bo.setGroupNameCn("控制系统");
        when(groupMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(groupMapper, never()).insert(any());
    }

    @Test
    void enabledGroupCannotBeEdited() {
        ProductMaterialTypeGroup current = new ProductMaterialTypeGroup();
        current.setGroupId(1L);
        current.setStatus("ENABLED");
        when(groupMapper.selectById(1L)).thenReturn(current);

        ProductMaterialTypeGroupBo bo = new ProductMaterialTypeGroupBo();
        bo.setGroupId(1L);
        bo.setGroupCode("CONTROL");
        bo.setGroupNameCn("控制系统");

        assertThatThrownBy(() -> service.updateByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(groupMapper, never()).updateById(any());
    }

    @Test
    void disabledGroupCanBeEdited() {
        ProductMaterialTypeGroup current = new ProductMaterialTypeGroup();
        current.setGroupId(1L);
        current.setStatus("DISABLED");
        when(groupMapper.selectById(1L)).thenReturn(current);
        when(groupMapper.updateById(any())).thenReturn(1);

        ProductMaterialTypeGroupBo bo = new ProductMaterialTypeGroupBo();
        bo.setGroupId(1L);
        bo.setGroupCode("CONTROL");
        bo.setGroupNameCn("控制系统");

        assertThat(service.updateByBo(bo)).isTrue();
    }

    @Test
    void referencedGroupCannotBeDeleted() {
        ProductMaterialTypeGroup current = new ProductMaterialTypeGroup();
        current.setGroupId(1L);
        current.setGroupCode("CONTROL");
        current.setStatus("DISABLED");
        when(groupMapper.selectById(1L)).thenReturn(current);
        when(typeMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[] {1L}))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void enabledGroupCannotBeDeleted() {
        ProductMaterialTypeGroup current = new ProductMaterialTypeGroup();
        current.setGroupId(1L);
        current.setStatus("ENABLED");
        when(groupMapper.selectById(1L)).thenReturn(current);

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[] {1L}))
            .isInstanceOf(ServiceException.class);
        verify(groupMapper, never()).deleteBatchIds(any());
    }

    @Test
    void checkReferencesCountsTypesAttributesAndMaterials() {
        ProductMaterialTypeGroup current = new ProductMaterialTypeGroup();
        current.setGroupId(1L);
        current.setGroupCode("CONTROL");
        when(groupMapper.selectById(1L)).thenReturn(current);
        when(typeMapper.selectCount(any())).thenReturn(2L);
        when(baseAttributeMapper.selectCount(any())).thenReturn(3L);
        when(materialMapper.selectCount(any())).thenReturn(4L);

        ReferenceCheckResultVo result = service.checkReferences(1L);

        assertThat(result.getReferenceCount()).isEqualTo(9L);
        assertThat(result.getAllowed()).isFalse();
    }

    @Test
    void lockedGroupEditCheckIsDenied() {
        ProductMaterialTypeGroup current = new ProductMaterialTypeGroup();
        current.setGroupId(1L);
        current.setGroupCode("CONTROL");
        current.setStatus("DISABLED");
        current.setSystemFlag(Boolean.TRUE);
        current.setEditableFlag(Boolean.FALSE);
        when(groupMapper.selectById(1L)).thenReturn(current);

        BaseEditCheckResultVo result = service.checkEditAllowed(1L);

        assertThat(result.getEditable()).isFalse();
        assertThat(result.getReasonKey()).isEqualTo("product.materialTypeGroup.notEditable");
    }
}
