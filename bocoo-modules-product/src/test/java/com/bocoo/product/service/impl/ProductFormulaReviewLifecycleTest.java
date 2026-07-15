package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.service.ProductFormulaSetupService;
import com.bocoo.product.service.ProductChangeLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductFormulaReviewLifecycleTest {

    @Mock
    private ProductFormulaMapper formulaMapper;
    @Mock
    private ProductFormulaVersionMapper versionMapper;
    @Mock
    private ProductFormulaSetupService setupService;
    @Mock
    private ProductChangeLogService changeLogService;
    @Mock
    private ProductFormulaSnapshotJson snapshotJson;
    @Mock
    private ProductFormulaVersionReferenceGuard versionReferenceGuard;

    private ProductFormulaReviewLifecycle lifecycle;

    @BeforeEach
    void setUp() {
        lifecycle = new ProductFormulaReviewLifecycle(formulaMapper, versionMapper, setupService,
            changeLogService, snapshotJson, versionReferenceGuard);
    }

    @Test
    void stopChecksEnabledSaleProductsByFormulaId() {
        ProductFormula formula = new ProductFormula();
        formula.setFormulaId(1L);
        formula.setCurrentVersionId(10L);
        formula.setStatus("EFFECTIVE");
        when(formulaMapper.selectActiveByIdForUpdate(1L)).thenReturn(formula);
        doThrow(ServiceException.ofMessageKey("product.formula.stopSaleProductEnabled"))
            .when(versionReferenceGuard).assertNoEnabledSaleProductByFormula(1L);

        assertThatThrownBy(() -> lifecycle.stop(1L)).isInstanceOf(ServiceException.class);

        verify(versionReferenceGuard).assertNoEnabledSaleProductByFormula(1L);
        verify(formulaMapper, never()).update(any(), any());
        verify(versionMapper, never()).update(any(), any());
    }
}
