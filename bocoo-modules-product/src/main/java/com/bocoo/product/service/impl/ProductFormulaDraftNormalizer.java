package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductFormulaBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductDictItemMapper;
import com.bocoo.product.mapper.ProductFormulaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ProductFormulaDraftNormalizer extends ProductServiceSupport {

    private static final String STATUS_DRAFT = ProductFormulaServiceImpl.STATUS_DRAFT;
    private static final String STATUS_PENDING_REVIEW = ProductFormulaServiceImpl.STATUS_PENDING_REVIEW;
    private static final String STATUS_REJECTED = ProductFormulaServiceImpl.STATUS_REJECTED;
    private static final String STATUS_EFFECTIVE = ProductFormulaServiceImpl.STATUS_EFFECTIVE;
    private static final String STATUS_STOPPED = ProductFormulaServiceImpl.STATUS_STOPPED;
    private static final String VALIDATION_NOT_VALIDATED = ProductFormulaServiceImpl.VALIDATION_NOT_VALIDATED;
    private static final String PRODUCT_TYPE_DICT = "product_type";

    private final ProductFormulaMapper formulaMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductDictItemMapper dictItemMapper;

    public void normalize(ProductFormulaBo bo, boolean insert) {
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
        normalizeSizeRange(bo);
        validateRequiredFields(bo);
        validateSizeRange(bo);
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
        defaultValidationStatus(bo);
        bo.setSizeSummary(buildSizeSummary(bo.getMinWidthInch(), bo.getMaxWidthInch(), bo.getMinHeightInch(), bo.getMaxHeightInch()));
        if (insert || StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(STATUS_DRAFT);
        } else {
            bo.setStatus(normalizeStatus(bo.getStatus()));
        }
    }

    public void validateUnique(ProductFormulaBo bo) {
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
            .eq("min_width_inch", bo.getMinWidthInch())
            .eq("min_height_inch", bo.getMinHeightInch())
            .eq("max_width_inch", bo.getMaxWidthInch())
            .eq("max_height_inch", bo.getMaxHeightInch())
            .ne(bo.getFormulaId() != null, "formula_id", bo.getFormulaId());
        if (formulaMapper.selectCount(naturalKeyQuery) > 0) {
            throw ServiceException.ofMessageKey("product.formula.naturalKeyExists");
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

    private void normalizeSizeRange(ProductFormulaBo bo) {
        if (bo.getMinWidthInch() == null) {
            bo.setMinWidthInch(BigDecimal.ZERO);
        }
        if (bo.getMinHeightInch() == null) {
            bo.setMinHeightInch(BigDecimal.ZERO);
        }
    }

    private void validateSizeRange(ProductFormulaBo bo) {
        if (bo.getMinWidthInch().compareTo(bo.getMaxWidthInch()) > 0) {
            throw ServiceException.ofMessageKey("product.formula.minWidthGreaterThanMax");
        }
        if (bo.getMinHeightInch().compareTo(bo.getMaxHeightInch()) > 0) {
            throw ServiceException.ofMessageKey("product.formula.minHeightGreaterThanMax");
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

    private void defaultValidationStatus(ProductFormulaBo bo) {
        if (StringUtils.isBlank(bo.getMaterialValidationStatus())) {
            bo.setMaterialValidationStatus(VALIDATION_NOT_VALIDATED);
        }
        if (StringUtils.isBlank(bo.getOptionValidationStatus())) {
            bo.setOptionValidationStatus(VALIDATION_NOT_VALIDATED);
        }
        if (StringUtils.isBlank(bo.getSimulationValidationStatus())) {
            bo.setSimulationValidationStatus(VALIDATION_NOT_VALIDATED);
        }
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

    private String buildSizeSummary(BigDecimal minWidth, BigDecimal maxWidth, BigDecimal minHeight, BigDecimal maxHeight) {
        return strip(minWidth) + "≤W≤" + strip(maxWidth) + "in, " + strip(minHeight) + "≤H≤" + strip(maxHeight) + "in";
    }

    private String strip(BigDecimal value) {
        return value == null ? "-" : value.stripTrailingZeros().toPlainString();
    }
}
