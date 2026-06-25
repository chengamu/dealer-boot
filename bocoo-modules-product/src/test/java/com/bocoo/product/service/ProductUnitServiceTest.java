package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductUnitBo;
import com.bocoo.product.domain.entity.ProductUnit;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductUnitMapper;
import com.bocoo.product.service.impl.ProductUnitServiceImpl;
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
class ProductUnitServiceTest {

    @Mock
    private ProductUnitMapper unitMapper;
    @Mock
    private ProductMaterialMapper materialMapper;
    @Mock
    private ProductMaterialAttributeMapper materialAttributeMapper;

    private ProductUnitServiceImpl productUnitService;

    @BeforeEach
    void setUp() {
        productUnitService = new ProductUnitServiceImpl(
            unitMapper,
            materialMapper,
            materialAttributeMapper
        );
    }

    @Test
    void unitReferenceCheckCountsEveryBusinessUsage() {
        ProductUnit unit = new ProductUnit();
        unit.setUnitId(3001L);
        unit.setUnitCode("MM");
        when(unitMapper.selectById(3001L)).thenReturn(unit);
        when(materialMapper.selectCount(any())).thenReturn(1L);
        when(materialAttributeMapper.selectCount(any())).thenReturn(4L);

        ReferenceCheckResultVo result = productUnitService.checkReferences(3001L);

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getReferenceCount()).isEqualTo(5L);
        assertThat(result.getBlockerReasonKey()).isEqualTo("product.unit.hasReferences");
        assertThat(result.getReferenceSummaries()).containsExactly("Unit code references: 5");
    }

    @Test
    void unitReferenceCheckAllowsUnknownUnit() {
        when(unitMapper.selectById(3001L)).thenReturn(null);

        ReferenceCheckResultVo result = productUnitService.checkReferences(3001L);

        assertThat(result.getAllowed()).isTrue();
        assertThat(result.getReferenceCount()).isZero();
        assertThat(result.getReferenceSummaries()).isEmpty();
    }

    @Test
    void insertUnitRejectsBaseUnitWithDifferentType() {
        ProductUnit baseUnit = new ProductUnit();
        baseUnit.setUnitCode("PCS");
        baseUnit.setUnitType("COUNT");
        when(unitMapper.selectOne(any())).thenReturn(baseUnit);

        ProductUnitBo bo = new ProductUnitBo();
        bo.setUnitCode("CM");
        bo.setUnitNameCn("厘米");
        bo.setUnitType("LENGTH");
        bo.setBaseUnitCode("PCS");

        assertThatThrownBy(() -> productUnitService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteUnitRejectsEnabledStatus() {
        ProductUnit unit = new ProductUnit();
        unit.setUnitId(3001L);
        unit.setStatus("ENABLED");
        when(unitMapper.selectById(3001L)).thenReturn(unit);

        assertThatThrownBy(() -> productUnitService.deleteWithValidByIds(new Long[]{3001L}))
            .isInstanceOf(ServiceException.class);
        verify(unitMapper, never()).deleteBatchIds(any());
    }
}
