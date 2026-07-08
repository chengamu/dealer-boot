package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.mapper.ProductPriceFabricMapper;
import com.bocoo.product.mapper.ProductPriceFabricRuleMapper;
import com.bocoo.product.mapper.ProductPriceFeeRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
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
    private ProductPriceFabricMapper priceFabricMapper;
    @Mock
    private ProductPriceFabricRuleMapper priceFabricRuleMapper;
    @Mock
    private ProductPriceFeeRuleMapper priceFeeRuleMapper;

    private ProductFormulaVersionReferenceGuard guard;

    @BeforeEach
    void setUp() {
        guard = new ProductFormulaVersionReferenceGuard(
            saleProductMapper,
            priceSettingMapper,
            priceFabricMapper,
            priceFabricRuleMapper,
            priceFeeRuleMapper
        );
    }

    @Test
    void noReferenceAllowsWithdraw() {
        when(saleProductMapper.selectCount(any())).thenReturn(0L);
        when(priceSettingMapper.selectCount(any())).thenReturn(0L);
        when(priceFabricMapper.selectCount(any())).thenReturn(0L);
        when(priceFabricRuleMapper.selectCount(any())).thenReturn(0L);
        when(priceFeeRuleMapper.selectCount(any())).thenReturn(0L);

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
        verify(priceFabricMapper, never()).selectCount(any());
    }
}
