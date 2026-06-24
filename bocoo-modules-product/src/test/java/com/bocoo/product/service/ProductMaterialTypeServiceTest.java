package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductMaterialTypeBo;
import com.bocoo.product.domain.entity.ProductMaterialType;
import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMaterialTypeGroupMapper;
import com.bocoo.product.mapper.ProductMaterialTypeMapper;
import com.bocoo.product.service.impl.ProductMaterialTypeServiceImpl;
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
class ProductMaterialTypeServiceTest {

    @Mock
    private ProductMaterialTypeMapper typeMapper;
    @Mock
    private ProductMaterialTypeGroupMapper groupMapper;
    @Mock
    private ProductMaterialMapper materialMapper;

    private ProductMaterialTypeServiceImpl service;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        service = new ProductMaterialTypeServiceImpl(typeMapper, groupMapper, materialMapper);
    }

    @Test
    void insertRequiresGroupAndFillsGroupSnapshot() {
        ProductMaterialTypeBo bo = new ProductMaterialTypeBo();
        bo.setMaterialTypeCode("motor");
        bo.setMaterialTypeNameCn("电机");
        bo.setAttributeGroupCode("CONTROL");
        when(groupMapper.selectOne(any())).thenReturn(group());
        when(typeMapper.insert(any())).thenReturn(1);

        assertThat(service.insertByBo(bo)).isTrue();
        assertThat(bo.getMaterialTypeCode()).isEqualTo("MOTOR");
        assertThat(bo.getAttributeGroupId()).isEqualTo(10L);
        assertThat(bo.getAttributeGroupNameCn()).isEqualTo("控制系统");
    }

    @Test
    void insertRejectsMissingGroup() {
        ProductMaterialTypeBo bo = new ProductMaterialTypeBo();
        bo.setMaterialTypeCode("MOTOR");
        bo.setMaterialTypeNameCn("电机");

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(typeMapper, never()).insert(any());
    }

    @Test
    void insertRejectsDuplicateMaterialTypeCode() {
        ProductMaterialTypeBo bo = new ProductMaterialTypeBo();
        bo.setMaterialTypeCode("MOTOR");
        bo.setMaterialTypeNameCn("电机");
        bo.setAttributeGroupCode("CONTROL");
        when(groupMapper.selectOne(any())).thenReturn(group());
        when(typeMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(typeMapper, never()).insert(any());
    }

    @Test
    void enabledTypeCannotBeEdited() {
        ProductMaterialType current = new ProductMaterialType();
        current.setMaterialTypeId(1L);
        current.setStatus("ENABLED");
        when(groupMapper.selectOne(any())).thenReturn(group());
        when(typeMapper.selectById(1L)).thenReturn(current);

        ProductMaterialTypeBo bo = new ProductMaterialTypeBo();
        bo.setMaterialTypeId(1L);
        bo.setMaterialTypeCode("MOTOR");
        bo.setMaterialTypeNameCn("电机");
        bo.setAttributeGroupCode("CONTROL");

        assertThatThrownBy(() -> service.updateByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(typeMapper, never()).updateById(any());
    }

    @Test
    void referencedTypeCannotBeDeleted() {
        ProductMaterialType current = new ProductMaterialType();
        current.setMaterialTypeId(1L);
        current.setMaterialTypeCode("FABRIC");
        when(typeMapper.selectById(1L)).thenReturn(current);
        when(materialMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.deleteWithValidByIds(new Long[] {1L}))
            .isInstanceOf(ServiceException.class);
    }

    private ProductMaterialTypeGroup group() {
        ProductMaterialTypeGroup group = new ProductMaterialTypeGroup();
        group.setGroupId(10L);
        group.setGroupCode("CONTROL");
        group.setGroupNameCn("控制系统");
        group.setDelFlag("0");
        return group;
    }
}
