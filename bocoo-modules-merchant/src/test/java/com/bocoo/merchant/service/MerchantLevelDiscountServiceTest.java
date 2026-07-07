package com.bocoo.merchant.service;

import com.bocoo.common.core.enums.TenantType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.bo.MerchantLevelDiscountBo;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import com.bocoo.merchant.mapper.MerchantLevelDiscountMapper;
import com.bocoo.merchant.mapper.MerchantLevelMapper;
import com.bocoo.merchant.service.impl.MerchantLevelDiscountServiceImpl;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductDictItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantLevelDiscountServiceTest {

    @Mock
    private MerchantLevelDiscountMapper discountMapper;
    @Mock
    private MerchantLevelMapper levelMapper;
    @Mock
    private ProductCategoryMapper categoryMapper;
    @Mock
    private ProductDictItemMapper dictItemMapper;

    private MerchantLevelDiscountServiceImpl service;

    @BeforeEach
    void setUp() {
        MerchantServiceTestSupport.prepare();
        TestSaTokenContext.install();
        TestSaTokenContext.setLoginUser(TenantType.PLATFORM.getCode(), 1L, 1L, "admin");
        service = new MerchantLevelDiscountServiceImpl(discountMapper, levelMapper, categoryMapper, dictItemMapper);
    }

    @Test
    void insertRejectsDuplicateNaturalKey() {
        mockValidRefs();
        when(discountMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> service.insertByBo(validBo())).isInstanceOf(ServiceException.class);
        verify(discountMapper, never()).insert(any());
    }

    @Test
    void insertRejectsInvalidProductType() {
        MerchantLevel level = new MerchantLevel();
        level.setLevelId(1L);
        level.setStatus("ENABLED");
        level.setDelFlag("0");
        when(levelMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(level);
        ProductCategory category = new ProductCategory();
        category.setCategoryId(2L);
        category.setStatus("ENABLED");
        category.setDelFlag("0");
        when(categoryMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(category);

        assertThatThrownBy(() -> service.insertByBo(validBo())).isInstanceOf(ServiceException.class);
    }

    @Test
    void updateStatusRejectsInvalidStatus() {
        assertThatThrownBy(() -> service.updateStatus(1L, "ARCHIVED")).isInstanceOf(ServiceException.class);
        verify(discountMapper, never()).update(any(), any());
    }

    private MerchantLevelDiscountBo validBo() {
        MerchantLevelDiscountBo bo = new MerchantLevelDiscountBo();
        bo.setLevelId(1L);
        bo.setCategoryId(2L);
        bo.setProductTypeCode("custom_curtain");
        bo.setDiscountRate(new BigDecimal("0.95"));
        return bo;
    }

    private void mockValidRefs() {
        MerchantLevel level = new MerchantLevel();
        level.setLevelId(1L);
        level.setLevelCode("VIP");
        level.setLevelName("VIP");
        level.setStatus("ENABLED");
        level.setDelFlag("0");
        when(levelMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(level);
        ProductCategory category = new ProductCategory();
        category.setCategoryId(2L);
        category.setCategoryCode("CURTAIN");
        category.setCategoryNameCn("窗帘");
        category.setStatus("ENABLED");
        category.setDelFlag("0");
        when(categoryMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(category);
        ProductDictItem item = new ProductDictItem();
        item.setDictItemValue("CUSTOM_CURTAIN");
        item.setDictItemLabelCn("定制帘");
        item.setStatus("ENABLED");
        item.setDelFlag("0");
        when(dictItemMapper.selectOne(any(), org.mockito.ArgumentMatchers.eq(false))).thenReturn(item);
    }
}
