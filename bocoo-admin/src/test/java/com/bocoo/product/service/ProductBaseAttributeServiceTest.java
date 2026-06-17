package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductBaseAttributeBo;
import com.bocoo.product.mapper.ProductBaseAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
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

@ExtendWith(MockitoExtension.class)
class ProductBaseAttributeServiceTest {

    @Mock
    private ProductBaseAttributeMapper baseAttributeMapper;
    @Mock
    private ProductMaterialAttributeMapper materialAttributeMapper;

    private ProductBaseAttributeServiceImpl productBaseAttributeService;

    @BeforeEach
    void setUp() {
        productBaseAttributeService = new ProductBaseAttributeServiceImpl(baseAttributeMapper, materialAttributeMapper);
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
    void normalizeBaseAttributeClearsUnitForNonNumberValueType() throws Exception {
        ProductBaseAttributeBo bo = new ProductBaseAttributeBo();
        bo.setAttributeGroup("FABRIC");
        bo.setAttributeCode("COLOR");
        bo.setAttributeNameCn("颜色");
        bo.setValueType("text");
        bo.setUnitCode("M");

        Method normalize = ProductBaseAttributeServiceImpl.class.getDeclaredMethod("normalizeBaseAttribute", ProductBaseAttributeBo.class);
        normalize.setAccessible(true);
        normalize.invoke(productBaseAttributeService, bo);

        assertThat(bo.getValueType()).isEqualTo("TEXT");
        assertThat(bo.getUnitCode()).isNull();
    }
}
