package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.mapper.ProductPriceMaterialMapper;
import com.bocoo.product.mapper.ProductPriceMaterialRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.service.impl.ProductSaleProductEnableGuard;
import com.bocoo.product.service.impl.ProductSaleProductServiceImpl;
import com.bocoo.product.service.impl.ProductQuoteReferenceGuard;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

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
    private ProductPriceMaterialMapper fabricMapper;
    @Mock
    private ProductPriceMaterialRuleMapper materialRuleMapper;
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
            fabricMapper,
            materialRuleMapper,
            formulaMapper,
            versionMapper,
            changeLogService,
            new ProductQuoteReferenceGuard(List.of()),
            new ProductSaleProductEnableGuard(formulaMapper, versionMapper)
        );
    }

    @Test
    void priceMustBeReadyBeforeSaleProductCanEnable() {
        ProductSaleProduct product = saleProduct("NOT_READY", "DISABLED");
        when(saleProductMapper.selectActiveByIdForUpdate(9001L)).thenReturn(product);

        assertThatThrownBy(() -> saleProductService.updateStatus(9001L, "ENABLED"))
            .isInstanceOf(ServiceException.class);

        verify(saleProductMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void readySaleProductCanEnable() {
        ProductSaleProduct product = saleProduct("READY", "DISABLED");
        when(saleProductMapper.selectActiveByIdForUpdate(9001L)).thenReturn(product);
        when(formulaMapper.selectActiveByIdForUpdate(3001L)).thenReturn(formula());
        when(versionMapper.selectById(7001L)).thenReturn(version(7001L, "EFFECTIVE"));
        when(saleProductMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        assertThat(saleProductService.updateStatus(9001L, "ENABLED")).isTrue();

        verify(changeLogService).record(eq("PRODUCT_PRICING"), eq("SALE_PRODUCT"), eq(9001L),
            eq("SP-001"), eq("ENABLE"), eq(product),
            argThat(after -> after instanceof ProductSaleProduct row && "ENABLED".equals(row.getStatus())), isNull());
    }

    @Test
    void stoppedFormulaBlocksReEnable() {
        ProductSaleProduct product = saleProduct("READY", "DISABLED");
        ProductFormula formula = formula();
        formula.setStatus("STOPPED");
        when(saleProductMapper.selectActiveByIdForUpdate(9001L)).thenReturn(product);
        when(formulaMapper.selectActiveByIdForUpdate(3001L)).thenReturn(formula);

        assertThatThrownBy(() -> saleProductService.updateStatus(9001L, "ENABLED"))
            .isInstanceOf(ServiceException.class);

        verify(versionMapper, never()).selectById(any());
        verify(saleProductMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void stoppedVersionBlocksReEnable() {
        ProductSaleProduct product = saleProduct("READY", "DISABLED");
        when(saleProductMapper.selectActiveByIdForUpdate(9001L)).thenReturn(product);
        when(formulaMapper.selectActiveByIdForUpdate(3001L)).thenReturn(formula());
        when(versionMapper.selectById(7001L)).thenReturn(version(7001L, "STOPPED"));

        assertThatThrownBy(() -> saleProductService.updateStatus(9001L, "ENABLED"))
            .isInstanceOf(ServiceException.class);

        verify(saleProductMapper, never()).update(isNull(), any(LambdaUpdateWrapper.class));
    }

    @Test
    void oldEffectiveVersionCanReEnableWithoutCurrentVersionMatch() {
        ProductSaleProduct product = saleProduct("READY", "DISABLED");
        ProductFormula formula = formula();
        formula.setCurrentVersionId(7002L);
        when(saleProductMapper.selectActiveByIdForUpdate(9001L)).thenReturn(product);
        when(formulaMapper.selectActiveByIdForUpdate(3001L)).thenReturn(formula);
        when(versionMapper.selectById(7001L)).thenReturn(version(7001L, "EFFECTIVE"));
        when(saleProductMapper.update(isNull(), any(LambdaUpdateWrapper.class))).thenReturn(1);

        assertThat(saleProductService.updateStatus(9001L, "ENABLED")).isTrue();

        verify(versionMapper).selectById(7001L);
        verify(versionMapper, never()).selectById(7002L);
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
        when(saleProductMapper.selectBatchIds(List.of(9001L))).thenReturn(List.of(product));
        when(settingMapper.selectList(any())).thenReturn(List.of(setting));
        when(materialRuleMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> saleProductService.deleteWithValidByIds(new Long[]{9001L}))
            .isInstanceOf(ServiceException.class);

        verify(saleProductMapper, never()).deleteBatchIds(any());
    }

    @Test
    void versionChangeKeepsHistoricalPriceSettings() {
        ProductSaleProduct current = saleProduct("READY", "DISABLED");
        ProductSaleProduct saved = saleProduct("NOT_READY", "DISABLED");
        saved.setFormulaVersionId(7002L);
        saved.setFormulaVersionLabel("V2");
        when(saleProductMapper.selectActiveByIdForUpdate(9001L)).thenReturn(current, saved);
        when(formulaMapper.selectById(3001L)).thenReturn(formula());
        when(versionMapper.selectById(7002L)).thenReturn(version());
        when(saleProductMapper.selectCount(any())).thenReturn(0L);
        when(saleProductMapper.updateById(any())).thenReturn(1);
        when(settingMapper.selectOne(any())).thenReturn(null);
        when(settingMapper.insert(any())).thenReturn(1);

        ProductSaleProductBo bo = saleProductBo();

        assertThat(saleProductService.updateByBo(bo)).isTrue();

        verify(settingMapper).selectOne(argThat(wrapper ->
            wrapper != null && wrapper.getSqlSegment().contains("formula_version_id")));
        verify(settingMapper).insert(argThat(setting ->
            setting != null && Long.valueOf(7002L).equals(setting.getFormulaVersionId())));
        verify(fabricMapper, never()).delete(any());
        verify(materialRuleMapper, never()).delete(any());
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

    private ProductSaleProductBo saleProductBo() {
        ProductSaleProductBo bo = new ProductSaleProductBo();
        bo.setSaleProductId(9001L);
        bo.setSaleProductCode("SP-001");
        bo.setSaleProductName("斑马帘");
        bo.setFormulaId(3001L);
        bo.setStatus("DISABLED");
        return bo;
    }

    private ProductFormula formula() {
        ProductFormula formula = new ProductFormula();
        formula.setFormulaId(3001L);
        formula.setTenantId(1L);
        formula.setFormulaCode("FM-001");
        formula.setFormulaName("斑马帘");
        formula.setCategoryId(1001L);
        formula.setCategoryCode("ZEBRA");
        formula.setCategoryNameCn("斑马帘");
        formula.setProductTypeCode("CUSTOM_CURTAIN");
        formula.setProductTypeNameCn("定制帘");
        formula.setCurrentVersionId(7002L);
        formula.setMinWidthInch(BigDecimal.ONE);
        formula.setMinHeightInch(BigDecimal.ONE);
        formula.setMaxWidthInch(BigDecimal.TEN);
        formula.setMaxHeightInch(BigDecimal.TEN);
        formula.setStatus("EFFECTIVE");
        formula.setDelFlag("0");
        return formula;
    }

    private ProductFormulaVersion version() {
        return version(7002L, "EFFECTIVE");
    }

    private ProductFormulaVersion version(Long versionId, String status) {
        ProductFormulaVersion version = new ProductFormulaVersion();
        version.setVersionId(versionId);
        version.setVersionNo(versionId != null && versionId.equals(7001L) ? 1 : 2);
        version.setVersionLabel(versionId != null && versionId.equals(7001L) ? "V1" : "V2");
        version.setVersionStatus(status);
        version.setDelFlag("0");
        return version;
    }
}
