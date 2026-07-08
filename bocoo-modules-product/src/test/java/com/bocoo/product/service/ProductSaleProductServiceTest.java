package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.mapper.ProductPriceFabricRuleMapper;
import com.bocoo.product.mapper.ProductPriceFeeRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.service.impl.ProductSaleProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductSaleProductServiceTest {

    @Mock
    private ProductSaleProductMapper saleProductMapper;
    @Mock
    private ProductPriceSettingMapper settingMapper;
    @Mock
    private ProductPriceFabricRuleMapper fabricRuleMapper;
    @Mock
    private ProductPriceFeeRuleMapper feeRuleMapper;
    @Mock
    private ProductFormulaMapper formulaMapper;
    @Mock
    private ProductFormulaVersionMapper versionMapper;
    @Mock
    private ProductChangeLogService changeLogService;

    private ProductSaleProductServiceImpl saleProductService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        saleProductService = new ProductSaleProductServiceImpl(
            saleProductMapper,
            settingMapper,
            fabricRuleMapper,
            feeRuleMapper,
            formulaMapper,
            versionMapper,
            changeLogService
        );
    }

    @Test
    void priceMustBeReadyBeforeSaleProductCanEnable() {
        ProductSaleProduct product = saleProduct("NOT_READY", "DISABLED");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);

        assertThatThrownBy(() -> saleProductService.updateStatus(9001L, "ENABLED"))
            .isInstanceOf(ServiceException.class);

        verify(saleProductMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void readySaleProductCanEnable() {
        ProductSaleProduct product = saleProduct("READY", "DISABLED");
        when(saleProductMapper.selectById(9001L)).thenReturn(product);
        when(saleProductMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        assertThat(saleProductService.updateStatus(9001L, "ENABLED")).isTrue();

        verify(changeLogService).record(eq("PRODUCT_PRICING"), eq("SALE_PRODUCT"), eq(9001L),
            eq("SP-001"), eq("ENABLE"), eq(product),
            argThat(after -> after instanceof ProductSaleProduct row && "ENABLED".equals(row.getStatus())), isNull());
    }

    @Test
    void invalidStatusIsRejected() {
        assertThatThrownBy(() -> saleProductService.updateStatus(9001L, "DRAFT"))
            .isInstanceOf(ServiceException.class);

        verify(saleProductMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void saleProductCodeIsRequired() {
        ProductSaleProductBo bo = new ProductSaleProductBo();
        bo.setFormulaId(3001L);
        bo.setSaleProductName("斑马帘");

        assertThatThrownBy(() -> saleProductService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);

        verify(saleProductMapper, never()).insert(any());
    }

    @Test
    void priceRulesBlockDeletion() {
        ProductSaleProduct product = saleProduct("READY", "DISABLED");
        ProductPriceSetting setting = new ProductPriceSetting();
        setting.setPriceSettingId(9101L);
        when(saleProductMapper.selectBatchIds(java.util.List.of(9001L))).thenReturn(java.util.List.of(product));
        when(settingMapper.selectOne(any())).thenReturn(setting);
        when(fabricRuleMapper.selectCount(any())).thenReturn(1L);
        when(feeRuleMapper.selectCount(any())).thenReturn(0L);

        assertThatThrownBy(() -> saleProductService.deleteWithValidByIds(new Long[]{9001L}))
            .isInstanceOf(ServiceException.class);

        verify(saleProductMapper, never()).deleteBatchIds(any());
    }

    private ProductSaleProduct saleProduct(String priceStatus, String status) {
        ProductSaleProduct product = new ProductSaleProduct();
        product.setSaleProductId(9001L);
        product.setSaleProductCode("SP-001");
        product.setSaleProductName("斑马帘");
        product.setFormulaId(3001L);
        product.setFormulaVersionId(7001L);
        product.setPriceStatus(priceStatus);
        product.setStatus(status);
        product.setDelFlag("0");
        return product;
    }
}
