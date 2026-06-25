package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductDictItemBo;
import com.bocoo.product.domain.bo.ProductDictTypeBo;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.product.domain.entity.ProductDictType;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductDictItemVo;
import com.bocoo.product.domain.vo.ProductDictOptionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductDictItemMapper;
import com.bocoo.product.mapper.ProductDictTypeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMediaAssetMapper;
import com.bocoo.product.mapper.ProductUnitMapper;
import com.bocoo.product.service.impl.ProductDictItemServiceImpl;
import com.bocoo.product.service.impl.ProductDictTypeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductDictServiceTest {

    @Mock
    private ProductDictTypeMapper dictTypeMapper;
    @Mock
    private ProductDictItemMapper dictItemMapper;
    @Mock
    private ProductUnitMapper unitMapper;
    @Mock
    private ProductMaterialMapper materialMapper;
    @Mock
    private ProductCategoryMapper categoryMapper;
    @Mock
    private ProductMediaAssetMapper mediaAssetMapper;
    private ProductDictTypeServiceImpl dictTypeService;
    private ProductDictItemServiceImpl dictItemService;

    @BeforeEach
    void setUp() {
        dictTypeService = new ProductDictTypeServiceImpl(dictTypeMapper, dictItemMapper);
        dictItemService = new ProductDictItemServiceImpl(
            dictTypeMapper,
            dictItemMapper,
            unitMapper,
            materialMapper,
            categoryMapper,
            mediaAssetMapper
        );
    }

    @Test
    void insertDictTypeRejectsDuplicateCode() {
        ProductDictTypeBo bo = new ProductDictTypeBo();
        bo.setDictTypeCode("product_business_type");
        when(dictTypeMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> dictTypeService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteDictTypeRejectsSystemTypeBeforeRemoving() {
        ProductDictType entity = new ProductDictType();
        entity.setDictTypeId(1001L);
        entity.setDictTypeCode("product_business_type");
        entity.setSystemFlag(Boolean.TRUE);
        when(dictTypeMapper.selectById(1001L)).thenReturn(entity);

        assertThatThrownBy(() -> dictTypeService.deleteWithValidByIds(new Long[]{1001L}))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void deleteDictTypeRejectsEnabledStatus() {
        ProductDictType entity = new ProductDictType();
        entity.setDictTypeId(1001L);
        entity.setDictTypeCode("product_business_type");
        entity.setStatus("ENABLED");
        when(dictTypeMapper.selectById(1001L)).thenReturn(entity);

        assertThatThrownBy(() -> dictTypeService.deleteWithValidByIds(new Long[]{1001L}))
            .isInstanceOf(ServiceException.class);
        verify(dictTypeMapper, never()).deleteBatchIds(any());
    }

    @Test
    void dictTypeReferenceCheckCountsItems() {
        ProductDictType entity = new ProductDictType();
        entity.setDictTypeId(1001L);
        entity.setDictTypeCode("product_business_type");
        when(dictTypeMapper.selectById(1001L)).thenReturn(entity);
        when(dictItemMapper.selectCount(any())).thenReturn(2L);

        ReferenceCheckResultVo result = dictTypeService.checkReferences(1001L);

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getReferenceCount()).isEqualTo(2L);
        assertThat(result.getBlockerReasonKey()).isEqualTo("product.dict.typeHasItems");
        assertThat(result.getReferenceSummaries()).containsExactly("Dictionary items: 2");
    }

    @Test
    void insertDictItemRequiresExistingTypeAndUniqueValue() {
        ProductDictItemBo bo = new ProductDictItemBo();
        bo.setDictTypeCode("product_business_type");
        bo.setDictItemValue("SALES");
        when(dictTypeMapper.selectCount(any())).thenReturn(1L);
        when(dictItemMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> dictItemService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
    }

    @Test
    void dictItemOptionsUseEnabledRowsFromProductDict() {
        ProductDictItemVo row = new ProductDictItemVo();
        row.setDictTypeCode("product_business_type");
        row.setDictItemValue("SALES");
        row.setDictItemLabelCn("销售");
        row.setDictItemLabelEn("Sales");
        row.setParentValue("BASE");
        when(dictItemMapper.selectVoList(any())).thenReturn(List.of(row));

        List<ProductDictOptionVo> options = dictItemService.queryOptionsByType("product_business_type");

        assertThat(options).singleElement().satisfies(option -> {
            assertThat(option.getValue()).isEqualTo("SALES");
            assertThat(option.getLabel()).isEqualTo("销售");
            assertThat(option.getLabelCn()).isEqualTo("销售");
            assertThat(option.getLabelEn()).isEqualTo("Sales");
            assertThat(option.getParentValue()).isEqualTo("BASE");
        });
    }

    @Test
    void dictItemReferenceCheckBlocksSystemItem() {
        ProductDictItem entity = new ProductDictItem();
        entity.setDictItemId(2001L);
        entity.setSystemFlag(Boolean.TRUE);
        when(dictItemMapper.selectById(2001L)).thenReturn(entity);

        ReferenceCheckResultVo result = dictItemService.checkReferences(2001L);

        assertThat(result.getAllowed()).isFalse();
        assertThat(result.getReferenceCount()).isEqualTo(1L);
        assertThat(result.getBlockerReasonKey()).isEqualTo("product.dict.systemItemCannotDelete");
        assertThat(result.getReferenceSummaries()).containsExactly("System dictionary item");
    }

    @Test
    void deleteDictItemRejectsEnabledStatus() {
        ProductDictItem entity = new ProductDictItem();
        entity.setDictItemId(2001L);
        entity.setStatus("ENABLED");
        when(dictItemMapper.selectById(2001L)).thenReturn(entity);

        assertThatThrownBy(() -> dictItemService.deleteWithValidByIds(new Long[]{2001L}))
            .isInstanceOf(ServiceException.class);
        verify(dictItemMapper, never()).deleteBatchIds(any());
    }

    @Test
    void dictTypeEditCheckRejectsEnabledType() {
        ProductDictType entity = new ProductDictType();
        entity.setDictTypeId(1001L);
        entity.setDictTypeCode("product_unit_type");
        entity.setStatus("ENABLED");
        when(dictTypeMapper.selectById(1001L)).thenReturn(entity);

        BaseEditCheckResultVo result = dictTypeService.checkEditAllowed(1001L);

        assertThat(result.getEditable()).isFalse();
        assertThat(result.getReasonKey()).isEqualTo("product.base.edit.enabledDenied");
    }

    @Test
    void dictTypeEditCheckRejectsLockedSystemTypeEvenWhenDisabled() {
        ProductDictType entity = new ProductDictType();
        entity.setDictTypeId(1001L);
        entity.setDictTypeCode("product_unit_type");
        entity.setStatus("DISABLED");
        entity.setSystemFlag(Boolean.TRUE);
        entity.setEditableFlag(Boolean.FALSE);
        when(dictTypeMapper.selectById(1001L)).thenReturn(entity);

        BaseEditCheckResultVo result = dictTypeService.checkEditAllowed(1001L);

        assertThat(result.getEditable()).isFalse();
        assertThat(result.getReasonKey()).isEqualTo("product.dict.notEditable");
    }

    @Test
    void updateStatusRejectsLockedSystemDictType() {
        ProductDictType entity = new ProductDictType();
        entity.setDictTypeId(1001L);
        entity.setSystemFlag(Boolean.TRUE);
        entity.setEditableFlag(Boolean.FALSE);
        when(dictTypeMapper.selectById(1001L)).thenReturn(entity);

        assertThatThrownBy(() -> dictTypeService.updateStatus(1001L, "DISABLED"))
            .isInstanceOf(ServiceException.class);
        verify(dictTypeMapper, never()).update(any(), any());
    }

    @Test
    void normalUpdateRejectsEnabledDictItem() {
        ProductDictItem current = new ProductDictItem();
        current.setDictItemId(2001L);
        current.setDictTypeCode("product_unit_type");
        current.setDictItemValue("LENGTH");
        current.setStatus("ENABLED");
        when(dictTypeMapper.selectCount(any())).thenReturn(1L);
        when(dictItemMapper.selectById(2001L)).thenReturn(current);

        ProductDictItemBo bo = new ProductDictItemBo();
        bo.setDictItemId(2001L);
        bo.setDictTypeCode("product_unit_type");
        bo.setDictItemValue("LENGTH");

        assertThatThrownBy(() -> dictItemService.updateByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(dictItemMapper, never()).updateById(any());
    }

    @Test
    void updateStatusRejectsLockedSystemDictItem() {
        ProductDictItem entity = new ProductDictItem();
        entity.setDictItemId(2001L);
        entity.setSystemFlag(Boolean.TRUE);
        entity.setEditableFlag(Boolean.FALSE);
        when(dictItemMapper.selectById(2001L)).thenReturn(entity);

        assertThatThrownBy(() -> dictItemService.updateStatus(2001L, "DISABLED"))
            .isInstanceOf(ServiceException.class);
        verify(dictItemMapper, never()).update(any(), any());
    }
}
