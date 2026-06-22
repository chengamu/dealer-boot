package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.mapper.FabricSeriesMapper;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
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
    private ProductComponentItemMapper componentItemMapper;
    @Mock
    private FabricSeriesMapper fabricSeriesMapper;
    @Mock
    private ProductMediaBindingMapper mediaBindingMapper;

    private ProductMaterialServiceImpl productMaterialService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        productMaterialService = new ProductMaterialServiceImpl(
            materialMapper,
            materialAttributeMapper,
            componentItemMapper,
            fabricSeriesMapper,
            mediaBindingMapper
        );
    }

    @Test
    void saveProductMaterialDefaultsSpecAndProcurementFields() throws Exception {
        ProductMaterialBo bo = new ProductMaterialBo();
        bo.setMaterialCode("FABRIC_CELLULAR_25_WHITE");
        bo.setMaterialNameCn("25mm 米色蜂巢面料");
        bo.setMaterialType("PROFILE");
        bo.setUnitCode("M");
        bo.setPrimarySpec("25mm");
        bo.setAttributeSummary("25mm / 米色 / 遮光");

        Method normalize = ProductMaterialServiceImpl.class.getDeclaredMethod("normalizeMaterial", ProductMaterialBo.class);
        normalize.setAccessible(true);
        normalize.invoke(productMaterialService, bo);

        assertThat(bo.getSpecSummary()).isEqualTo("25mm / 米色 / 遮光");
        assertThat(bo.getPurchaseUnitCode()).isEqualTo("M");
        assertThat(bo.getInventoryUnitCode()).isEqualTo("M");
        assertThat(bo.getUsageUnitCode()).isEqualTo("M");
        assertThat(bo.getPurchaseEnabled()).isFalse();
        assertThat(bo.getInventoryEnabled()).isFalse();
        assertThat(bo.getPriceCurrencyCode()).isEqualTo("CNY");
    }

    @Test
    void editCheckRejectsEnabledMaterialBeforeOpeningEditor() {
        ProductMaterial entity = new ProductMaterial();
        entity.setMaterialId(1001L);
        entity.setStatus("ENABLED");
        when(materialMapper.selectById(1001L)).thenReturn(entity);

        BaseEditCheckResultVo result = productMaterialService.checkEditAllowed(1001L);

        assertThat(result.getEditable()).isFalse();
        assertThat(result.getReasonKey()).isEqualTo("product.base.edit.enabledDenied");
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

        assertThatThrownBy(() -> productMaterialService.updateByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(materialMapper, never()).updateById(any());
    }

    @Test
    void superUpdateAllowsEnabledMaterial() {
        ProductMaterialBo bo = new ProductMaterialBo();
        bo.setMaterialId(1001L);
        bo.setMaterialCode("MOTOR_001");
        bo.setMaterialNameCn("电机");
        when(materialMapper.updateById(any())).thenReturn(1);

        assertThat(productMaterialService.superUpdateByBo(bo)).isTrue();
        verify(materialMapper, never()).selectById(1001L);
    }
}
