package com.bocoo.product.service;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.product.domain.bo.ProductFormulaBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductDictItemMapper;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.service.impl.ProductFormulaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductFormulaServiceTest {

    @Mock
    private ProductFormulaMapper formulaMapper;
    @Mock
    private ProductFormulaVersionMapper versionMapper;
    @Mock
    private ProductCategoryMapper categoryMapper;
    @Mock
    private ProductDictItemMapper dictItemMapper;
    @Mock
    private ProductFormulaSetupService setupService;
    @Mock
    private ProductChangeLogService changeLogService;

    private ProductFormulaServiceImpl formulaService;

    @BeforeEach
    void setUp() {
        ProductServiceTestSupport.prepareMapperAndConverter();
        formulaService = new ProductFormulaServiceImpl(
            formulaMapper,
            versionMapper,
            categoryMapper,
            dictItemMapper,
            setupService,
            changeLogService
        );
    }

    @Test
    void insertFillsSnapshotsDefaultsAndRecordsChangeLog() {
        ProductFormulaBo bo = validFormulaBo();
        when(categoryMapper.selectById(1001L)).thenReturn(category());
        when(dictItemMapper.selectOne(any())).thenReturn(productType());
        when(formulaMapper.selectCount(any())).thenReturn(0L, 0L);
        doAnswer(invocation -> {
            ProductFormula entity = invocation.getArgument(0);
            entity.setFormulaId(3001L);
            return 1;
        }).when(formulaMapper).insert(any());

        assertThat(formulaService.insertByBo(bo)).isTrue();

        verify(formulaMapper).insert(argThat(entity ->
            "FORMULA_25_ZEBRA".equals(entity.getFormulaCode())
                && "ZEBRA".equals(entity.getCategoryCode())
                && "定制帘".equals(entity.getProductTypeNameCn())
                && "DRAFT".equals(entity.getStatus())
                && Boolean.FALSE.equals(entity.getConfiguredFlag())
                && Integer.valueOf(0).equals(entity.getMaterialLineCount())
                && Integer.valueOf(1).equals(entity.getDraftVersionNo())
                && "NOT_VALIDATED".equals(entity.getLatestValidationStatus())
                && "W≤25in, H≤72in".equals(entity.getSizeSummary())
        ));
        verify(changeLogService).record(eq("FORMULA"), eq("FORMULA"), eq(3001L), eq("FORMULA_25_ZEBRA"), eq("CREATE"), isNull(), any(), isNull());
    }

    @Test
    void insertRejectsDuplicateFormulaCode() {
        ProductFormulaBo bo = validFormulaBo();
        when(categoryMapper.selectById(1001L)).thenReturn(category());
        when(dictItemMapper.selectOne(any())).thenReturn(productType());
        when(formulaMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> formulaService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(formulaMapper, never()).insert(any());
    }

    @Test
    void insertRejectsDuplicateNaturalKey() {
        ProductFormulaBo bo = validFormulaBo();
        when(categoryMapper.selectById(1001L)).thenReturn(category());
        when(dictItemMapper.selectOne(any())).thenReturn(productType());
        when(formulaMapper.selectCount(any())).thenReturn(0L, 1L);

        assertThatThrownBy(() -> formulaService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(formulaMapper, never()).insert(any());
    }

    @Test
    void insertRejectsMissingOrDisabledProductTypeDictionaryItem() {
        ProductFormulaBo bo = validFormulaBo();
        when(categoryMapper.selectById(1001L)).thenReturn(category());
        when(dictItemMapper.selectOne(any())).thenReturn(null);

        assertThatThrownBy(() -> formulaService.insertByBo(bo))
            .isInstanceOf(ServiceException.class);
        verify(formulaMapper, never()).insert(any());
    }

    @Test
    void pendingReviewCannotBeEditedOrDeleted() {
        ProductFormula current = formula(3001L, "PENDING_REVIEW");
        when(categoryMapper.selectById(1001L)).thenReturn(category());
        when(dictItemMapper.selectOne(any())).thenReturn(productType());
        when(formulaMapper.selectCount(any())).thenReturn(0L, 0L);
        when(formulaMapper.selectById(3001L)).thenReturn(current);

        ProductFormulaBo bo = validFormulaBo();
        bo.setFormulaId(3001L);

        assertThatThrownBy(() -> formulaService.updateByBo(bo))
            .isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> formulaService.deleteWithValidByIds(new Long[]{3001L}))
            .isInstanceOf(ServiceException.class);
        verify(formulaMapper, never()).updateById(any());
        verify(formulaMapper, never()).deleteBatchIds(any());
    }

    @Test
    void rejectedFormulaCanBeEditedAndReturnsToDraftWithValidationReset() {
        ProductFormula current = formula(3001L, "REJECTED");
        current.setRejectReason("尺寸不清晰");
        when(categoryMapper.selectById(1001L)).thenReturn(category());
        when(dictItemMapper.selectOne(any())).thenReturn(productType());
        when(formulaMapper.selectCount(any())).thenReturn(0L, 0L);
        when(formulaMapper.selectById(3001L)).thenReturn(current);
        when(formulaMapper.updateById(any())).thenReturn(1);

        ProductFormulaBo bo = validFormulaBo();
        bo.setFormulaId(3001L);

        assertThat(formulaService.updateByBo(bo)).isTrue();
        verify(formulaMapper).updateById(argThat(entity ->
            "DRAFT".equals(entity.getStatus())
                && entity.getRejectReason() == null
                && "NOT_VALIDATED".equals(entity.getLatestValidationStatus())
        ));
        verify(changeLogService).record(eq("FORMULA"), eq("FORMULA"), eq(3001L), eq("FORMULA_25_ZEBRA"), eq("UPDATE"), eq(current), any(), isNull());
    }

    @Test
    void submitReviewRequiresConfigurationAndValidationPass() {
        ProductFormula notConfigured = formula(3001L, "DRAFT");
        ProductFormula notValidated = configuredFormula("DRAFT", "NOT_VALIDATED");
        ProductFormula valid = configuredFormula("DRAFT", "PASS");
        when(formulaMapper.selectById(3001L)).thenReturn(notConfigured, notValidated, valid);
        when(formulaMapper.update(any(), any())).thenReturn(1);

        assertThatThrownBy(() -> formulaService.submitReview(3001L))
            .isInstanceOf(ServiceException.class);
        assertThatThrownBy(() -> formulaService.submitReview(3001L))
            .isInstanceOf(ServiceException.class);
        assertThat(formulaService.submitReview(3001L)).isTrue();

        verify(changeLogService).record(eq("FORMULA"), eq("FORMULA"), eq(3001L), eq("FORMULA_25_ZEBRA"), eq("SUBMIT_REVIEW"), eq(valid),
            argThat(after -> after instanceof ProductFormula formula && "PENDING_REVIEW".equals(formula.getStatus())),
            isNull());
    }

    @Test
    void validateFormulaDelegatesToSetupService() {
        when(setupService.validateSetup(3001L)).thenReturn(Boolean.TRUE);

        assertThat(formulaService.validateFormula(3001L)).isTrue();

        verify(setupService).validateSetup(3001L);
    }

    @Test
    void approvePendingReviewCreatesVersionV1AndWritesAuditSnapshot() {
        ProductFormula current = configuredFormula("PENDING_REVIEW", "PASS");
        when(formulaMapper.selectById(3001L)).thenReturn(current);
        when(setupService.materialCount(3001L)).thenReturn(1);
        when(setupService.validationMessageKey(3001L)).thenReturn(null);
        when(setupService.snapshot(3001L)).thenReturn(Map.of("materials", List.of("MAT001")));
        when(versionMapper.selectOne(any())).thenReturn(null);
        doAnswer(invocation -> {
            ProductFormulaVersion version = invocation.getArgument(0);
            version.setVersionId(9001L);
            return 1;
        }).when(versionMapper).insert(any());
        when(formulaMapper.update(any(), any())).thenReturn(1);

        assertThat(formulaService.approve(3001L)).isTrue();

        verify(versionMapper).insert(argThat(version ->
            Integer.valueOf(1).equals(version.getVersionNo())
                && "V1".equals(version.getVersionLabel())
                && "EFFECTIVE".equals(version.getVersionStatus())
                && version.getSetupSnapshotJson() != null
        ));
        verify(changeLogService).record(eq("FORMULA"), eq("FORMULA"), eq(3001L), eq("FORMULA_25_ZEBRA"), eq("APPROVE_VERSION"), eq(current),
            argThat(after -> after instanceof ProductFormula formula
                && "EFFECTIVE".equals(formula.getStatus())
                && Long.valueOf(9001L).equals(formula.getCurrentVersionId())
                && Integer.valueOf(1).equals(formula.getCurrentVersionNo())
                && "V1".equals(formula.getCurrentVersionLabel())),
            isNull());
    }

    @Test
    void approveSecondVersionGeneratesV2() {
        ProductFormula current = configuredFormula("PENDING_REVIEW", "PASS");
        ProductFormulaVersion latest = new ProductFormulaVersion();
        latest.setVersionNo(1);
        when(formulaMapper.selectById(3001L)).thenReturn(current);
        when(setupService.materialCount(3001L)).thenReturn(1);
        when(setupService.validationMessageKey(3001L)).thenReturn(null);
        when(setupService.snapshot(3001L)).thenReturn(Map.of("materials", List.of("MAT001")));
        when(versionMapper.selectOne(any())).thenReturn(latest);
        doAnswer(invocation -> {
            ProductFormulaVersion version = invocation.getArgument(0);
            version.setVersionId(9002L);
            return 1;
        }).when(versionMapper).insert(any());
        when(formulaMapper.update(any(), any())).thenReturn(1);

        assertThat(formulaService.approve(3001L)).isTrue();

        verify(versionMapper).insert(argThat(version ->
            Integer.valueOf(2).equals(version.getVersionNo())
                && "V2".equals(version.getVersionLabel())
        ));
    }

    @Test
    void effectiveFormulaCanBeStoppedButCannotBeReEnabledOrDeleted() {
        ProductFormula current = formula(3001L, "EFFECTIVE");
        current.setCurrentVersionId(9001L);
        when(formulaMapper.selectById(3001L)).thenReturn(current, formula(3001L, "STOPPED"));
        when(formulaMapper.update(any(), any())).thenReturn(1);
        when(versionMapper.update(any(), any())).thenReturn(1);

        assertThat(formulaService.stop(3001L)).isTrue();
        assertThatThrownBy(() -> formulaService.deleteWithValidByIds(new Long[]{3001L}))
            .isInstanceOf(ServiceException.class);

        verify(changeLogService).record(eq("FORMULA"), eq("FORMULA"), eq(3001L), eq("FORMULA_25_ZEBRA"), eq("STOP"), eq(current), any(), isNull());
    }

    private ProductFormulaBo validFormulaBo() {
        ProductFormulaBo bo = new ProductFormulaBo();
        bo.setFormulaCode("FORMULA_25_ZEBRA");
        bo.setFormulaName("25英寸 9.5斑马帘标准配方");
        bo.setCategoryId(1001L);
        bo.setProductTypeCode("CUSTOM_CURTAIN");
        bo.setMaxWidthInch(new BigDecimal("25"));
        bo.setMaxHeightInch(new BigDecimal("72"));
        return bo;
    }

    private ProductCategory category() {
        ProductCategory category = new ProductCategory();
        category.setCategoryId(1001L);
        category.setCategoryCode("ZEBRA");
        category.setCategoryNameCn("斑马帘");
        category.setStatus("ENABLED");
        category.setDelFlag("0");
        return category;
    }

    private ProductDictItem productType() {
        ProductDictItem item = new ProductDictItem();
        item.setDictItemId(2001L);
        item.setDictTypeCode("product_type");
        item.setDictItemValue("CUSTOM_CURTAIN");
        item.setDictItemLabelCn("定制帘");
        item.setStatus("ENABLED");
        item.setDelFlag("0");
        return item;
    }

    private ProductFormula configuredFormula(String status, String validationStatus) {
        ProductFormula formula = formula(3001L, status);
        formula.setConfiguredFlag(Boolean.TRUE);
        formula.setMaterialLineCount(1);
        formula.setLatestValidationStatus(validationStatus);
        return formula;
    }

    private ProductFormula formula(Long id, String status) {
        ProductFormula formula = new ProductFormula();
        formula.setFormulaId(id);
        formula.setFormulaCode("FORMULA_25_ZEBRA");
        formula.setFormulaName("25英寸 9.5斑马帘标准配方");
        formula.setStatus(status);
        formula.setConfiguredFlag(Boolean.FALSE);
        formula.setMaterialLineCount(0);
        formula.setDraftVersionNo(1);
        formula.setDelFlag("0");
        return formula;
    }
}
