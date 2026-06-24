package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMaterialType;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.mapper.ProductMaterialTypeMapper;
import com.bocoo.product.service.impl.ProductMaterialServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ProductMaterialServiceTest {

    @Mock
    private ProductMaterialMapper materialMapper;
    @Mock
    private ProductMaterialAttributeMapper materialAttributeMapper;
    @Mock
    private ProductMaterialTypeMapper materialTypeMapper;
    @Mock
    private ProductMediaBindingMapper mediaBindingMapper;
    @Mock
    private ProductChangeLogService changeLogService;

    private ProductMaterialServiceImpl productMaterialService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        productMaterialService = new ProductMaterialServiceImpl(
            materialMapper,
            materialAttributeMapper,
            materialTypeMapper,
            mediaBindingMapper,
            changeLogService
        );
    }

    @Test
    void normalizeProductMaterialFillsTypeSnapshotAndSpecModelText() throws Exception {
        ProductMaterialBo bo = new ProductMaterialBo();
        bo.setMaterialCode("FABRIC_CELLULAR_25_WHITE");
        bo.setMaterialNameCn("25mm 米色蜂巢面料");
        bo.setMaterialTypeCode("PROFILE");
        bo.setUnitCode("M");
        bo.setModel("HC-IV-25");
        bo.setSpec("25mm / 米色 / 遮光");
        when(materialTypeMapper.selectOne(any())).thenReturn(materialType("PROFILE", "铝材", "ALUMINUM", "铝材"));

        Method normalize = ProductMaterialServiceImpl.class.getDeclaredMethod("normalizeMaterial", ProductMaterialBo.class);
        normalize.setAccessible(true);
        normalize.invoke(productMaterialService, bo);

        assertThat(bo.getAttributeGroupCode()).isEqualTo("ALUMINUM");
        assertThat(bo.getSpecModelText()).isEqualTo("型号：HC-IV-25；规格：25mm / 米色 / 遮光");
        assertThat(bo.getStatus()).isEqualTo("DISABLED");
    }

    @Test
    void editCheckRejectsEnabledMaterialBeforeOpeningEditor() {
        ProductMaterial entity = new ProductMaterial();
        entity.setMaterialId(1001L);
        entity.setStatus("ENABLED");
        when(materialMapper.selectById(1001L)).thenReturn(entity);

        BaseEditCheckResultVo result = productMaterialService.checkEditAllowed(1001L);

        assertThat(result.getEditable()).isFalse();
        assertThat(result.getReasonKey()).isEqualTo("product.material.auditedEditDenied");
    }

    @Test
    void normalUpdateRejectsEnabledMaterial() {
        ProductMaterial current = new ProductMaterial();
        current.setMaterialId(1001L);
        current.setStatus("ENABLED");
        when(materialMapper.selectById(1001L)).thenReturn(current);

        ProductMaterialBo bo = new ProductMaterialBo();
        bo.setMaterialId(1001L);
        bo.setMaterialCode("MOTOR_001");
        bo.setMaterialNameCn("电机");
        bo.setMaterialTypeCode("MOTOR");
        bo.setUnitCode("PCS");
        bo.setSpec("45mm");
        when(materialTypeMapper.selectOne(any())).thenReturn(materialType("MOTOR", "电机", "CONTROL", "控制系统"));

        assertThatThrownBy(() -> productMaterialService.updateByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(materialMapper, never()).updateById(any());
    }

    @Test
    void superUpdateAllowsEnabledMaterial() {
        ProductMaterial current = new ProductMaterial();
        current.setMaterialId(1001L);
        current.setMaterialCode("MOTOR_001");
        current.setMaterialNameCn("电机");
        current.setStatus("ENABLED");
        when(materialMapper.selectById(1001L)).thenReturn(current);

        ProductMaterialBo bo = new ProductMaterialBo();
        bo.setMaterialId(1001L);
        bo.setMaterialCode("MOTOR_001");
        bo.setMaterialNameCn("电机");
        bo.setMaterialTypeCode("MOTOR");
        bo.setUnitCode("PCS");
        bo.setSpec("45mm");
        when(materialTypeMapper.selectOne(any())).thenReturn(materialType("MOTOR", "电机", "CONTROL", "控制系统"));
        when(materialMapper.updateById(any())).thenReturn(1);

        assertThat(productMaterialService.superUpdateByBo(bo)).isTrue();
        verify(changeLogService).record(
            org.mockito.ArgumentMatchers.eq("BASE_INFO"),
            org.mockito.ArgumentMatchers.eq("MATERIAL"),
            org.mockito.ArgumentMatchers.eq(1001L),
            org.mockito.ArgumentMatchers.eq("MOTOR_001"),
            org.mockito.ArgumentMatchers.eq("SUPER_UPDATE"),
            org.mockito.ArgumentMatchers.eq(current),
            any(),
            org.mockito.ArgumentMatchers.isNull()
        );
    }

    @Test
    void insertRejectsDuplicateMaterialCode() {
        ProductMaterialBo bo = validMaterialBo();
        when(materialTypeMapper.selectOne(any())).thenReturn(materialType("MOTOR", "电机", "CONTROL", "控制系统"));
        when(materialMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productMaterialService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(materialMapper, never()).insert(any());
    }

    @Test
    void insertRejectsDuplicateNaturalKeyWithManufacturer() {
        ProductMaterialBo bo = validMaterialBo();
        bo.setManufacturerName("AOK Motion");
        when(materialTypeMapper.selectOne(any())).thenReturn(materialType("MOTOR", "电机", "CONTROL", "控制系统"));
        when(materialMapper.selectCount(any())).thenReturn(0L, 1L);

        assertThatThrownBy(() -> productMaterialService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(materialMapper, never()).insert(any());
    }

    @Test
    void insertRejectsDuplicateNaturalKeyWithoutManufacturer() {
        ProductMaterialBo bo = validMaterialBo();
        bo.setManufacturerName(null);
        when(materialTypeMapper.selectOne(any())).thenReturn(materialType("MOTOR", "电机", "CONTROL", "控制系统"));
        when(materialMapper.selectCount(any())).thenReturn(0L, 1L);

        assertThatThrownBy(() -> productMaterialService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(materialMapper, never()).insert(any());
    }

    @Test
    void insertRejectsMissingSpec() {
        ProductMaterialBo bo = validMaterialBo();
        bo.setSpec(null);

        assertThatThrownBy(() -> productMaterialService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(materialMapper, never()).insert(any());
    }

    @Test
    void changeStatusAuditsAndUnauditsMaterial() {
        ProductMaterial current = new ProductMaterial();
        current.setMaterialId(1001L);
        current.setMaterialCode("MOTOR_001");
        current.setMaterialNameCn("电机");
        current.setStatus("DISABLED");
        when(materialMapper.selectById(1001L)).thenReturn(current);
        when(materialMapper.update(any(), any())).thenReturn(1);

        assertThat(productMaterialService.updateStatus(1001L, "ENABLED")).isTrue();
        assertThat(productMaterialService.updateStatus(1001L, "DISABLED")).isTrue();

        verify(materialMapper, org.mockito.Mockito.times(2)).update(any(), any());
        verify(changeLogService).record(
            org.mockito.ArgumentMatchers.eq("BASE_INFO"),
            org.mockito.ArgumentMatchers.eq("MATERIAL"),
            org.mockito.ArgumentMatchers.eq(1001L),
            org.mockito.ArgumentMatchers.eq("MOTOR_001"),
            org.mockito.ArgumentMatchers.eq("AUDIT"),
            any(),
            any(),
            org.mockito.ArgumentMatchers.isNull()
        );
        verify(changeLogService).record(
            org.mockito.ArgumentMatchers.eq("BASE_INFO"),
            org.mockito.ArgumentMatchers.eq("MATERIAL"),
            org.mockito.ArgumentMatchers.eq(1001L),
            org.mockito.ArgumentMatchers.eq("MOTOR_001"),
            org.mockito.ArgumentMatchers.eq("UNAUDIT"),
            any(),
            any(),
            org.mockito.ArgumentMatchers.isNull()
        );
    }

    @Test
    void materialReferencesBlockDeleteButAllowUnaudit() {
        when(materialAttributeMapper.selectCount(any())).thenReturn(1L);
        when(mediaBindingMapper.selectCount(any())).thenReturn(1L);

        ReferenceCheckResultVo result = productMaterialService.checkReferences(1001L);

        assertThat(result.getReferenceCount()).isEqualTo(2L);
        assertThat(result.getCanRemove()).isFalse();
        assertThat(result.getCanDisable()).isTrue();
        assertThat(result.getBlockerReasonKey()).isEqualTo("product.material.hasReferences");
    }

    @Test
    void deleteRejectsReferencedMaterial() {
        when(materialAttributeMapper.selectCount(any())).thenReturn(1L);
        when(mediaBindingMapper.selectCount(any())).thenReturn(0L);

        assertThatThrownBy(() -> productMaterialService.deleteWithValidByIds(new Long[] {1001L}))
            .isInstanceOf(ServiceException.class);
        verify(materialMapper, never()).deleteBatchIds(any());
    }

    private ProductMaterialBo validMaterialBo() {
        ProductMaterialBo bo = new ProductMaterialBo();
        bo.setMaterialCode("MOTOR_001");
        bo.setMaterialNameCn("电机");
        bo.setMaterialTypeCode("MOTOR");
        bo.setUnitCode("PCS");
        bo.setModel("AOK-45");
        bo.setSpec("45mm");
        return bo;
    }

    private ProductMaterialType materialType(String code, String name, String groupCode, String groupName) {
        ProductMaterialType type = new ProductMaterialType();
        type.setMaterialTypeId(2001L);
        type.setMaterialTypeCode(code);
        type.setMaterialTypeNameCn(name);
        type.setAttributeGroupId(3001L);
        type.setAttributeGroupCode(groupCode);
        type.setAttributeGroupNameCn(groupName);
        type.setDelFlag("0");
        return type;
    }
}
