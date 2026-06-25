package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductManufacturerBo;
import com.bocoo.product.domain.entity.ProductManufacturer;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductManufacturerMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.service.impl.ProductManufacturerServiceImpl;
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
class ProductManufacturerServiceTest {

    @Mock
    private ProductManufacturerMapper manufacturerMapper;
    @Mock
    private ProductMaterialMapper materialMapper;

    private ProductManufacturerServiceImpl productManufacturerService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        productManufacturerService = new ProductManufacturerServiceImpl(
            manufacturerMapper,
            materialMapper
        );
    }

    @Test
    void insertRejectsDuplicateManufacturerCode() {
        ProductManufacturerBo bo = validManufacturerBo();
        when(manufacturerMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productManufacturerService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(manufacturerMapper, never()).insert(any());
    }

    @Test
    void deleteRejectsEnabledManufacturer() {
        ProductManufacturer current = new ProductManufacturer();
        current.setManufacturerId(900L);
        current.setStatus("ENABLED");
        when(manufacturerMapper.selectById(900L)).thenReturn(current);

        assertThatThrownBy(() -> productManufacturerService.deleteWithValidByIds(new Long[] {900L}))
            .isInstanceOf(ServiceException.class);
        verify(manufacturerMapper, never()).deleteBatchIds(any());
    }

    @Test
    void deleteRejectsReferencedManufacturer() {
        ProductManufacturer current = new ProductManufacturer();
        current.setManufacturerId(900L);
        current.setStatus("DISABLED");
        when(manufacturerMapper.selectById(900L)).thenReturn(current);
        when(materialMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> productManufacturerService.deleteWithValidByIds(new Long[] {900L}))
            .isInstanceOf(ServiceException.class);
        verify(manufacturerMapper, never()).deleteBatchIds(any());
    }

    @Test
    void manufacturerReferenceCheckCountsMaterials() {
        when(materialMapper.selectCount(any())).thenReturn(2L);

        ReferenceCheckResultVo result = productManufacturerService.checkReferences(900L);

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getReferenceCount()).isEqualTo(2L);
        assertThat(result.getBlockerReasonKey()).isEqualTo("product.manufacturer.hasReferences");
    }

    private ProductManufacturerBo validManufacturerBo() {
        ProductManufacturerBo bo = new ProductManufacturerBo();
        bo.setManufacturerCode("900");
        bo.setManufacturerName("通用厂家/暂无厂家");
        return bo;
    }
}
