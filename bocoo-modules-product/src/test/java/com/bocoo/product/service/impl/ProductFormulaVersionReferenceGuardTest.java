package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.service.ProductQuoteReferenceProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductFormulaVersionReferenceGuardTest {

    @Mock
    private ProductSaleProductMapper saleProductMapper;
    @Mock
    private ProductPriceSettingMapper priceSettingMapper;
    @Mock
    private ProductPriceMaterialMapper priceMaterialMapper;
    @Mock
    private ProductPriceMaterialRuleMapper priceMaterialRuleMapper;
    @Mock
    private ProductQuoteReferenceProvider quoteReferenceProvider;

    private ProductFormulaVersionReferenceGuard guard;

    @BeforeEach
    void setUp() {
        guard = new ProductFormulaVersionReferenceGuard(
            saleProductMapper,
            priceSettingMapper,
            priceMaterialMapper,
            priceMaterialRuleMapper,
            new ProductQuoteReferenceGuard(java.util.List.of(quoteReferenceProvider))
        );
    }

    @Test
    void noReferenceAllowsWithdraw() {
        when(saleProductMapper.selectCount(any())).thenReturn(0L);
        when(priceSettingMapper.selectCount(any())).thenReturn(0L);
        when(priceMaterialMapper.selectCount(any())).thenReturn(0L);
        when(priceMaterialRuleMapper.selectCount(any())).thenReturn(0L);
        assertThatCode(() -> guard.assertNoBusinessReference(9001L))
            .doesNotThrowAnyException();
    }

    @Test
    void saleProductReferenceBlocksWithdraw() {
        when(saleProductMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> guard.assertNoBusinessReference(9001L))
            .isInstanceOf(ServiceException.class);
        verify(priceSettingMapper, never()).selectCount(any());
    }

    @Test
    void priceSettingReferenceBlocksWithdraw() {
        when(saleProductMapper.selectCount(any())).thenReturn(0L);
        when(priceSettingMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> guard.assertNoBusinessReference(9001L))
            .isInstanceOf(ServiceException.class);
        verify(priceMaterialMapper, never()).selectCount(any());
    }

    @Test
    void quoteReferenceBlocksWithdraw() {
        when(saleProductMapper.selectCount(any())).thenReturn(0L);
        when(priceSettingMapper.selectCount(any())).thenReturn(0L);
        when(priceMaterialMapper.selectCount(any())).thenReturn(0L);
        when(priceMaterialRuleMapper.selectCount(any())).thenReturn(0L);
        when(quoteReferenceProvider.countFormulaVersionReferences(9001L)).thenReturn(1L);

        assertThatThrownBy(() -> guard.assertNoBusinessReference(9001L))
            .isInstanceOf(ServiceException.class);
    }
}
