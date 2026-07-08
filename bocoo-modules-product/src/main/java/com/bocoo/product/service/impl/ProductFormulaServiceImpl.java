package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductFormulaBo;
import com.bocoo.product.domain.bo.ProductFormulaReviewBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductFormulaReviewRecordVo;
import com.bocoo.product.domain.vo.ProductFormulaVersionVo;
import com.bocoo.product.domain.vo.ProductFormulaVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductFormulaService;
import com.bocoo.product.service.ProductFormulaSetupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductFormulaServiceImpl extends ProductServiceSupport implements ProductFormulaService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_REVIEW = "PENDING_REVIEW";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_EFFECTIVE = "EFFECTIVE";
    public static final String STATUS_STOPPED = "STOPPED";
    public static final String VALIDATION_NOT_VALIDATED = "NOT_VALIDATED";
    public static final String VALIDATION_PASS = "PASS";

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaSetupService setupService;
    private final ProductChangeLogService changeLogService;
    private final ProductFormulaDraftNormalizer draftNormalizer;
    private final ProductFormulaReviewLifecycle reviewLifecycle;
    private final ProductFormulaRevisionLifecycle revisionLifecycle;
    private final ProductFormulaReviewQueryService reviewQueryService;
    private final ProductFormulaReviewRecordQueryService reviewRecordQueryService;

    @Override
    public TableDataInfo<ProductFormulaVo> queryPageList(ProductFormulaBo bo, PageQuery pageQuery) {
        return page(formulaMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByDesc("update_time"));
    }

    @Override
    public List<ProductFormulaVo> queryList(ProductFormulaBo bo) {
        return formulaMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByDesc("update_time")));
    }

    @Override
    public ProductFormulaVo queryById(Long id) {
        return formulaMapper.selectVoById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ProductFormulaBo bo) {
        draftNormalizer.normalize(bo, true);
        draftNormalizer.validateUnique(bo);
        ProductFormula entity = MapstructUtils.convert(bo, ProductFormula.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        boolean inserted = formulaMapper.insert(entity) > 0;
        if (inserted) {
            recordFormulaChange(entity.getFormulaId(), entity.getFormulaCode(), "CREATE", null, entity);
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ProductFormulaBo bo) {
        draftNormalizer.normalize(bo, false);
        draftNormalizer.validateUnique(bo);
        ProductFormula current = formulaMapper.selectById(bo.getFormulaId());
        assertFormulaEditable(current);
        ProductFormula entity = MapstructUtils.convert(bo, ProductFormula.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        entity.setStatus(STATUS_DRAFT);
        entity.setRejectReason(null);
        entity.setLatestValidationStatus(VALIDATION_NOT_VALIDATED);
        entity.setLatestValidationMessage(null);
        entity.setLatestValidationTime(null);
        entity.setMaterialValidationStatus(VALIDATION_NOT_VALIDATED);
        entity.setMaterialValidationMessage(null);
        entity.setMaterialValidationTime(null);
        entity.setOptionValidationStatus(VALIDATION_NOT_VALIDATED);
        entity.setOptionValidationMessage(null);
        entity.setOptionValidationTime(null);
        entity.setSimulationValidationStatus(VALIDATION_NOT_VALIDATED);
        entity.setSimulationValidationMessage(null);
        entity.setSimulationValidationTime(null);
        boolean updated = formulaMapper.updateById(entity) > 0;
        if (updated) {
            recordFormulaChange(entity.getFormulaId(), entity.getFormulaCode(), "UPDATE", current, entity);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Long[] ids) {
        List<ProductFormula> formulas = formulaMapper.selectBatchIds(Arrays.asList(ids));
        for (ProductFormula current : formulas) {
            if (current != null && !STATUS_DRAFT.equals(current.getStatus())) {
                throw ServiceException.ofMessageKey("product.formula.deleteOnlyDraft");
            }
            assertNoReferences(checkReferences(current.getFormulaId()));
        }
        for (ProductFormula current : formulas) {
            if (current != null) {
                recordFormulaChange(current.getFormulaId(), current.getFormulaCode(), "DELETE", current, null);
            }
        }
        return remove(formulaMapper, ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitReview(Long id) {
        return reviewLifecycle.submitReview(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(Long id) {
        return reviewLifecycle.approve(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(Long id, String rejectReason) {
        return reviewLifecycle.reject(id, rejectReason);
    }

    @Override
    public TableDataInfo<ProductFormulaVersionVo> queryReviewPage(ProductFormulaReviewBo bo, PageQuery pageQuery) {
        return reviewQueryService.queryReviewPage(bo, pageQuery);
    }

    @Override
    public ProductFormulaVersionVo queryReviewById(Long reviewId) {
        return reviewLifecycle.queryReviewById(reviewId);
    }

    @Override
    public List<ProductFormulaReviewRecordVo> queryReviewRecords(Long reviewId) {
        return reviewRecordQueryService.queryReviewRecords(reviewId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approveReview(Long reviewId) {
        return reviewLifecycle.approveReview(reviewId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean rejectReview(Long reviewId, String rejectReason) {
        return reviewLifecycle.rejectReview(reviewId, rejectReason);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stop(Long id) {
        return reviewLifecycle.stop(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean startRevision(Long id) {
        return revisionLifecycle.startRevision(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean withdraw(Long id) {
        return revisionLifecycle.withdraw(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductFormulaVo copyFormula(Long id, ProductFormulaBo bo) {
        return revisionLifecycle.copyFormula(id, bo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean validateFormula(Long id) {
        return setupService.validateSetup(id);
    }

    @Override
    public List<ProductFormulaVersionVo> queryVersions(Long formulaId) {
        return reviewLifecycle.queryVersions(formulaId);
    }

    @Override
    public List<ProductFormulaVersionVo> queryVersionHistory(Long formulaId) {
        return revisionLifecycle.queryVersionHistory(formulaId);
    }

    @Override
    public ProductFormulaVersionVo queryVersionById(Long formulaId, Long versionId) {
        return reviewLifecycle.queryVersionById(formulaId, versionId);
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductFormula formula = formulaMapper.selectById(id);
        if (formula == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        if (!isEditableStatus(formula.getStatus())) {
            return deniedEditCheck(formula.getStatus(), "product.formula.editDenied", checkReferences(id));
        }
        return editCheckResult(formula.getStatus(), checkReferences(id));
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long formulaId) {
        ProductFormula formula = formulaMapper.selectById(formulaId);
        ReferenceCheckResultVo result = referenceResult(0, null, null);
        if (formula == null) {
            result.setCanDisable(Boolean.FALSE);
            return result;
        }
        boolean canRemove = STATUS_DRAFT.equals(formula.getStatus());
        result.setAllowed(canRemove);
        result.setCanRemove(canRemove);
        result.setCanDisable(STATUS_EFFECTIVE.equals(formula.getStatus()));
        if (!canRemove) {
            result.setBlockerReasonKey("product.formula.deleteOnlyDraft");
        }
        return result;
    }

    private QueryWrapper<ProductFormula> buildQueryWrapper(ProductFormulaBo bo) {
        QueryWrapper<ProductFormula> q = activeQuery(ProductFormula.class);
        if (bo != null) {
            like(q, "formula_code", bo.getFormulaCode());
            like(q, "formula_name", bo.getFormulaName());
            eq(q, "category_id", bo.getCategoryId());
            like(q, "category_code", bo.getCategoryCode());
            eq(q, "product_type_code", bo.getProductTypeCode());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void assertFormulaEditable(ProductFormula current) {
        if (current == null) {
            throw ServiceException.ofMessageKey("product.base.edit.notFound");
        }
        if (!isEditableStatus(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.editDenied");
        }
    }

    private boolean isEditableStatus(String status) {
        return STATUS_DRAFT.equals(status) || STATUS_REJECTED.equals(status);
    }

    private void recordFormulaChange(Long formulaId, String formulaCode, String actionType,
                                     Object before, Object after) {
        changeLogService.record("FORMULA", "FORMULA", formulaId, formulaCode, actionType, before, after, null);
    }


}
