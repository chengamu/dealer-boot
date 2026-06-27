package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.product.domain.bo.ProductFormulaBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductFormulaVersionVo;
import com.bocoo.product.domain.vo.ProductFormulaVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductDictItemMapper;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductFormulaService;
import com.bocoo.product.service.ProductFormulaSetupService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductFormulaServiceImpl extends ProductServiceSupport implements ProductFormulaService {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PENDING_REVIEW = "PENDING_REVIEW";
    public static final String STATUS_REJECTED = "REJECTED";
    public static final String STATUS_EFFECTIVE = "EFFECTIVE";
    public static final String STATUS_STOPPED = "STOPPED";
    private static final String VALIDATION_NOT_VALIDATED = "NOT_VALIDATED";
    private static final String VALIDATION_PASS = "PASS";
    private static final String VALIDATION_FAIL = "FAIL";
    private static final String PRODUCT_TYPE_DICT = "product_type";
    private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaVersionMapper versionMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductDictItemMapper dictItemMapper;
    private final ProductFormulaSetupService setupService;
    private final ProductChangeLogService changeLogService;

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
        normalizeFormula(bo, true);
        validateFormulaUnique(bo);
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
        normalizeFormula(bo, false);
        validateFormulaUnique(bo);
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
        boolean updated = formulaMapper.updateById(entity) > 0;
        if (updated) {
            recordFormulaChange(entity.getFormulaId(), entity.getFormulaCode(), "UPDATE", current, entity);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            ProductFormula current = formulaMapper.selectById(id);
            if (current != null && !STATUS_DRAFT.equals(current.getStatus())) {
                throw ServiceException.ofMessageKey("product.formula.deleteOnlyDraft");
            }
            assertNoReferences(checkReferences(id));
        }
        for (Long id : ids) {
            ProductFormula current = formulaMapper.selectById(id);
            if (current != null) {
                recordFormulaChange(current.getFormulaId(), current.getFormulaCode(), "DELETE", current, null);
            }
        }
        return remove(formulaMapper, ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean submitReview(Long id) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_DRAFT.equals(current.getStatus()) && !STATUS_REJECTED.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.submitReviewDenied");
        }
        if (!Boolean.TRUE.equals(current.getConfiguredFlag()) || current.getMaterialLineCount() == null || current.getMaterialLineCount() <= 0) {
            throw ServiceException.ofMessageKey("product.formula.notConfigured");
        }
        if (!VALIDATION_PASS.equals(current.getLatestValidationStatus())) {
            throw ServiceException.ofMessageKey("product.formula.validationRequired");
        }
        return updateFormulaStatus(current, STATUS_PENDING_REVIEW, "SUBMIT_REVIEW", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean approve(Long id) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_PENDING_REVIEW.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.approveDenied");
        }
        if (setupService.materialCount(id) <= 0) {
            throw ServiceException.ofMessageKey("product.formula.notConfigured");
        }
        String setupMessageKey = setupService.validationMessageKey(id);
        if (setupMessageKey != null) {
            throw ServiceException.ofMessageKey(setupMessageKey);
        }
        String auditBy = currentUsername();
        LocalDateTime auditTime = TimeUtils.utcNow();
        ProductFormulaVersion version = buildApprovedVersion(current, setupService.snapshot(id), auditBy, auditTime);
        ProductEntityDefaults.prepareInsert(version);
        boolean inserted = versionMapper.insert(version) > 0;
        if (!inserted) {
            return Boolean.FALSE;
        }
        ProductFormula after = copyStatusSnapshot(current);
        after.setStatus(STATUS_EFFECTIVE);
        after.setAuditBy(auditBy);
        after.setAuditTime(auditTime);
        after.setRejectReason(null);
        after.setCurrentVersionId(version.getVersionId());
        after.setCurrentVersionNo(version.getVersionNo());
        after.setCurrentVersionLabel(version.getVersionLabel());
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, id)
            .set(ProductFormula::getStatus, STATUS_EFFECTIVE)
            .set(ProductFormula::getAuditBy, auditBy)
            .set(ProductFormula::getAuditTime, auditTime)
            .set(ProductFormula::getRejectReason, null)
            .set(ProductFormula::getCurrentVersionId, version.getVersionId())
            .set(ProductFormula::getCurrentVersionNo, version.getVersionNo())
            .set(ProductFormula::getCurrentVersionLabel, version.getVersionLabel())) > 0;
        if (updated) {
            recordFormulaChange(id, current.getFormulaCode(), "APPROVE_VERSION", current, after);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(Long id, String rejectReason) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_PENDING_REVIEW.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.rejectDenied");
        }
        if (StringUtils.isBlank(rejectReason)) {
            throw ServiceException.ofMessageKey("product.formula.rejectReasonRequired");
        }
        return updateFormulaStatus(current, STATUS_REJECTED, "REJECT", rejectReason.trim());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean stop(Long id) {
        ProductFormula current = requireFormula(id);
        if (!STATUS_EFFECTIVE.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.stopDenied");
        }
        ProductFormula after = copyStatusSnapshot(current);
        after.setStatus(STATUS_STOPPED);
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, id)
            .set(ProductFormula::getStatus, STATUS_STOPPED)) > 0;
        if (updated) {
            markCurrentVersionStopped(current);
            recordFormulaChange(id, current.getFormulaCode(), "STOP", current, after);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean validateFormula(Long id) {
        return setupService.validateSetup(id);
    }

    @Override
    public List<ProductFormulaVersionVo> queryVersions(Long formulaId) {
        return versionMapper.selectVoList(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .orderByDesc("version_no", "version_id"));
    }

    @Override
    public ProductFormulaVersionVo queryVersionById(Long formulaId, Long versionId) {
        return versionMapper.selectVoOne(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .eq("version_id", versionId));
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
        ReferenceCheckResultVo result = referenceResult(0, null, null);
        result.setCanDisable(Boolean.TRUE);
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

    private void normalizeFormula(ProductFormulaBo bo, boolean insert) {
        if (bo == null) {
            throw ServiceException.ofMessageKey("product.formula.required");
        }
        bo.setFormulaCode(trimToNull(bo.getFormulaCode()));
        bo.setFormulaName(trimToNull(bo.getFormulaName()));
        bo.setCategoryCode(trimToNull(bo.getCategoryCode()));
        bo.setCategoryNameCn(trimToNull(bo.getCategoryNameCn()));
        bo.setProductTypeCode(trimToNull(bo.getProductTypeCode()));
        bo.setProductTypeNameCn(trimToNull(bo.getProductTypeNameCn()));
        bo.setSizeSummary(trimToNull(bo.getSizeSummary()));
        bo.setStatus(trimToNull(bo.getStatus()));
        bo.setRejectReason(trimToNull(bo.getRejectReason()));
        bo.setRemark(trimToNull(bo.getRemark()));
        validateRequiredFields(bo);
        normalizeCategory(bo);
        normalizeProductType(bo);
        if (bo.getConfiguredFlag() == null) {
            bo.setConfiguredFlag(Boolean.FALSE);
        }
        if (bo.getMaterialLineCount() == null) {
            bo.setMaterialLineCount(0);
        }
        if (bo.getDraftVersionNo() == null) {
            bo.setDraftVersionNo(1);
        }
        if (StringUtils.isBlank(bo.getLatestValidationStatus())) {
            bo.setLatestValidationStatus(VALIDATION_NOT_VALIDATED);
        }
        if (StringUtils.isBlank(bo.getSizeSummary())) {
            bo.setSizeSummary(buildSizeSummary(bo.getMaxWidthInch(), bo.getMaxHeightInch()));
        }
        if (insert || StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(STATUS_DRAFT);
        } else {
            bo.setStatus(normalizeStatus(bo.getStatus()));
        }
    }

    private void validateRequiredFields(ProductFormulaBo bo) {
        if (StringUtils.isBlank(bo.getFormulaCode())) {
            throw ServiceException.ofMessageKey("product.formula.codeRequired");
        }
        if (StringUtils.isBlank(bo.getFormulaName())) {
            throw ServiceException.ofMessageKey("product.formula.nameRequired");
        }
        if (bo.getCategoryId() == null && StringUtils.isBlank(bo.getCategoryCode())) {
            throw ServiceException.ofMessageKey("product.formula.categoryRequired");
        }
        if (StringUtils.isBlank(bo.getProductTypeCode())) {
            throw ServiceException.ofMessageKey("product.formula.productTypeRequired");
        }
        if (bo.getMaxWidthInch() == null || bo.getMaxHeightInch() == null) {
            throw ServiceException.ofMessageKey("product.formula.sizeRequired");
        }
    }

    private void normalizeCategory(ProductFormulaBo bo) {
        ProductCategory category = null;
        if (bo.getCategoryId() != null) {
            category = categoryMapper.selectById(bo.getCategoryId());
            if (category != null && !"0".equals(category.getDelFlag())) {
                category = null;
            }
        }
        if (category == null && StringUtils.isNotBlank(bo.getCategoryCode())) {
            category = categoryMapper.selectOne(activeQuery(ProductCategory.class).eq("category_code", bo.getCategoryCode()));
        }
        if (category == null || !STATUS_ENABLED.equals(category.getStatus())) {
            throw ServiceException.ofMessageKey("product.formula.categoryNotFound");
        }
        bo.setCategoryId(category.getCategoryId());
        bo.setCategoryCode(category.getCategoryCode());
        bo.setCategoryNameCn(category.getCategoryNameCn());
    }

    private void normalizeProductType(ProductFormulaBo bo) {
        ProductDictItem item = dictItemMapper.selectOne(activeQuery(ProductDictItem.class)
            .eq("dict_type_code", PRODUCT_TYPE_DICT)
            .eq("dict_item_value", bo.getProductTypeCode())
            .eq("status", STATUS_ENABLED));
        if (item == null) {
            throw ServiceException.ofMessageKey("product.formula.productTypeNotFound");
        }
        bo.setProductTypeCode(item.getDictItemValue());
        bo.setProductTypeNameCn(item.getDictItemLabelCn());
    }

    private void validateFormulaUnique(ProductFormulaBo bo) {
        QueryWrapper<ProductFormula> codeQuery = activeQuery(ProductFormula.class)
            .eq("formula_code", bo.getFormulaCode())
            .ne(bo.getFormulaId() != null, "formula_id", bo.getFormulaId());
        if (formulaMapper.selectCount(codeQuery) > 0) {
            throw ServiceException.ofMessageKey("product.formula.codeExists");
        }
        QueryWrapper<ProductFormula> naturalKeyQuery = activeQuery(ProductFormula.class)
            .eq("formula_name", bo.getFormulaName())
            .eq("category_id", bo.getCategoryId())
            .eq("product_type_code", bo.getProductTypeCode())
            .eq("max_width_inch", bo.getMaxWidthInch())
            .eq("max_height_inch", bo.getMaxHeightInch())
            .ne(bo.getFormulaId() != null, "formula_id", bo.getFormulaId());
        if (formulaMapper.selectCount(naturalKeyQuery) > 0) {
            throw ServiceException.ofMessageKey("product.formula.naturalKeyExists");
        }
    }

    private Boolean updateFormulaStatus(ProductFormula current, String status, String actionType, String rejectReason) {
        ProductFormula after = copyStatusSnapshot(current);
        after.setStatus(status);
        after.setRejectReason(rejectReason);
        boolean updated = formulaMapper.update(null, new LambdaUpdateWrapper<ProductFormula>()
            .eq(ProductFormula::getFormulaId, current.getFormulaId())
            .set(ProductFormula::getStatus, status)
            .set(ProductFormula::getRejectReason, rejectReason)) > 0;
        if (updated) {
            recordFormulaChange(current.getFormulaId(), current.getFormulaCode(), actionType, current, after);
        }
        return updated;
    }

    private ProductFormulaVersion buildApprovedVersion(ProductFormula current, Map<String, Object> setupSnapshot, String auditBy, LocalDateTime auditTime) {
        int versionNo = nextVersionNo(current.getFormulaId());
        ProductFormulaVersion version = new ProductFormulaVersion();
        version.setTenantId(current.getTenantId());
        version.setFormulaId(current.getFormulaId());
        version.setVersionNo(versionNo);
        version.setVersionLabel(versionLabel(versionNo));
        version.setVersionStatus(STATUS_EFFECTIVE);
        version.setFormulaSnapshotJson(toJson(current));
        version.setSetupSnapshotJson(toJson(setupSnapshot));
        version.setValidationStatus(VALIDATION_PASS);
        version.setValidationReportJson(toJson(Map.of("status", VALIDATION_PASS, "message", "product.formula.validationPassed")));
        version.setSubmitBy(current.getUpdateBy());
        version.setSubmitTime(current.getUpdateTime());
        version.setAuditBy(auditBy);
        version.setAuditTime(auditTime);
        return version;
    }

    private int nextVersionNo(Long formulaId) {
        ProductFormulaVersion latest = versionMapper.selectOne(activeQuery(ProductFormulaVersion.class)
            .eq("formula_id", formulaId)
            .orderByDesc("version_no")
            .last("limit 1"));
        return latest == null || latest.getVersionNo() == null ? 1 : latest.getVersionNo() + 1;
    }

    private void markCurrentVersionStopped(ProductFormula current) {
        if (current.getCurrentVersionId() == null) {
            return;
        }
        versionMapper.update(null, new LambdaUpdateWrapper<ProductFormulaVersion>()
            .eq(ProductFormulaVersion::getVersionId, current.getCurrentVersionId())
            .set(ProductFormulaVersion::getVersionStatus, STATUS_STOPPED));
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

    private ProductFormula requireFormula(Long id) {
        ProductFormula current = id == null ? null : formulaMapper.selectById(id);
        if (current == null) {
            throw ServiceException.ofMessageKey("product.base.edit.notFound");
        }
        return current;
    }

    private String normalizeStatus(String status) {
        String next = StringUtils.blankToDefault(status, STATUS_DRAFT).trim().toUpperCase(Locale.ROOT);
        if (STATUS_DRAFT.equals(next) || STATUS_PENDING_REVIEW.equals(next) || STATUS_REJECTED.equals(next)
            || STATUS_EFFECTIVE.equals(next) || STATUS_STOPPED.equals(next)) {
            return next;
        }
        throw ServiceException.ofMessageKey("product.formula.statusInvalid");
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String buildSizeSummary(BigDecimal width, BigDecimal height) {
        return "W≤" + strip(width) + "in, H≤" + strip(height) + "in";
    }

    private String strip(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }

    private String versionLabel(Integer versionNo) {
        return "V" + versionNo;
    }

    private String currentUsername() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception ignored) {
            return "system";
        }
    }

    private void recordFormulaChange(Long formulaId, String formulaCode, String actionType,
                                     Object before, Object after) {
        changeLogService.record("FORMULA", "FORMULA", formulaId, formulaCode, actionType, before, after, null);
    }

    private ProductFormula copyStatusSnapshot(ProductFormula source) {
        ProductFormula target = new ProductFormula();
        target.setFormulaId(source.getFormulaId());
        target.setFormulaCode(source.getFormulaCode());
        target.setFormulaName(source.getFormulaName());
        target.setStatus(source.getStatus());
        target.setMaterialLineCount(source.getMaterialLineCount());
        target.setConfiguredFlag(source.getConfiguredFlag());
        target.setCurrentVersionId(source.getCurrentVersionId());
        target.setCurrentVersionNo(source.getCurrentVersionNo());
        target.setCurrentVersionLabel(source.getCurrentVersionLabel());
        target.setDraftVersionNo(source.getDraftVersionNo());
        target.setLatestValidationStatus(source.getLatestValidationStatus());
        target.setLatestValidationMessage(source.getLatestValidationMessage());
        target.setLatestValidationTime(source.getLatestValidationTime());
        target.setAuditBy(source.getAuditBy());
        target.setAuditTime(source.getAuditTime());
        target.setRejectReason(source.getRejectReason());
        return target;
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            if (value instanceof ProductFormula formula) {
                Map<String, Object> snapshot = new LinkedHashMap<>();
                snapshot.put("formulaId", formula.getFormulaId());
                snapshot.put("formulaCode", formula.getFormulaCode());
                snapshot.put("formulaName", formula.getFormulaName());
                snapshot.put("categoryId", formula.getCategoryId());
                snapshot.put("categoryCode", formula.getCategoryCode());
                snapshot.put("categoryNameCn", formula.getCategoryNameCn());
                snapshot.put("productTypeCode", formula.getProductTypeCode());
                snapshot.put("productTypeNameCn", formula.getProductTypeNameCn());
                snapshot.put("maxWidthInch", formula.getMaxWidthInch());
                snapshot.put("maxHeightInch", formula.getMaxHeightInch());
                snapshot.put("sizeSummary", formula.getSizeSummary());
                return OBJECT_MAPPER.writeValueAsString(snapshot);
            }
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize formula snapshot", e);
        }
    }
}
