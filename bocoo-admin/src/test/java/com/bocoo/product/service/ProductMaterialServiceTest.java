package com.bocoo.product.service;

import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.mapper.FabricProfileMapper;
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

@ExtendWith(MockitoExtension.class)
class ProductMaterialServiceTest {

    @Mock
    private ProductMaterialMapper materialMapper;
    @Mock
    private ProductMaterialAttributeMapper materialAttributeMapper;
    @Mock
    private ProductComponentItemMapper componentItemMapper;
    @Mock
    private FabricProfileMapper fabricProfileMapper;
    @Mock
    private ProductMediaBindingMapper mediaBindingMapper;

    private ProductMaterialServiceImpl productMaterialService;

    @BeforeEach
    void setUp() {
        productMaterialService = new ProductMaterialServiceImpl(
            materialMapper,
            materialAttributeMapper,
            componentItemMapper,
            fabricProfileMapper,
            mediaBindingMapper
        );
    }

    @Test
    void saveProductMaterialDefaultsSpecAndProcurementFields() throws Exception {
        ProductMaterialBo bo = new ProductMaterialBo();
        bo.setMaterialCode("FABRIC_CELLULAR_25_WHITE");
        bo.setMaterialNameCn("25mm 米色蜂巢面料");
        bo.setMaterialType("FABRIC");
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
    }
}
