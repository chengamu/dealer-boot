package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.entity.ProductFormula;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.entity.ProductPriceFabric;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import com.bocoo.product.domain.entity.ProductSaleProduct;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import com.bocoo.product.mapper.ProductPriceFabricMapper;
import com.bocoo.product.mapper.ProductPriceFabricRuleMapper;
import com.bocoo.product.mapper.ProductPriceSettingMapper;
import com.bocoo.product.mapper.ProductSaleProductMapper;
import com.bocoo.product.service.ProductChangeLogService;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductSaleProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductSaleProductServiceImpl extends ProductServiceSupport implements ProductSaleProductService {

    static final String PRICE_NOT_READY = "NOT_READY";
    static final String PRICE_READY = "READY";
    static final String STATUS_EFFECTIVE = ProductFormulaServiceImpl.STATUS_EFFECTIVE;

    private final ProductSaleProductMapper saleProductMapper;
    private final ProductPriceSettingMapper settingMapper;
    private final ProductPriceFabricMapper fabricMapper;
    private final ProductPriceFabricRuleMapper fabricRuleMapper;
    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaVersionMapper versionMapper;
    private final ProductChangeLogService changeLogService;

    @Override
    public TableDataInfo<ProductSaleProductVo> queryPageList(ProductSaleProductBo bo, PageQuery pageQuery) {
        return page(saleProductMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByDesc("update_time"));
    }

    @Override
    public List<ProductSaleProductVo> queryList(ProductSaleProductBo bo) {
        return saleProductMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByDesc("update_time")));
    }

    @Override
    public ProductSaleProductVo queryById(Long id) {
        return saleProductMapper.selectVoById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ProductSaleProductBo bo) {
        ProductFormula formula = normalizeByFormula(bo);
        validateUnique(bo);
        ProductSaleProduct entity = ProductSaleProductEntityFactory.toEntity(bo);
        entity.setPriceStatus(PRICE_NOT_READY);
        ProductEntityDefaults.prepareInsert(entity);
        boolean inserted = saleProductMapper.insert(entity) > 0;
        if (inserted) {
            ensurePriceSetting(entity, formula);
            recordChange(entity.getSaleProductId(), entity.getSaleProductCode(), "CREATE", null, entity);
        }
        return inserted;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ProductSaleProductBo bo) {
        ProductSaleProduct current = requireSaleProduct(bo.getSaleProductId());
        assertEditable(current);
        ProductFormula formula = normalizeByFormula(bo);
        validateUnique(bo);
        ProductSaleProduct entity = ProductSaleProductEntityFactory.toEntity(bo);
        boolean versionChanged = !Objects.equals(current.getFormulaVersionId(), entity.getFormulaVersionId());
        if (versionChanged) {
            entity.setPriceStatus(PRICE_NOT_READY);
        }
        boolean updated = saleProductMapper.updateById(entity) > 0;
        if (updated) {
            ProductSaleProduct saved = requireSaleProduct(entity.getSaleProductId());
            ensurePriceSetting(saved, formula);
            recordChange(saved.getSaleProductId(), saved.getSaleProductCode(), "UPDATE", current, saved);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Long[] ids) {
        List<ProductSaleProduct> products = saleProductMapper.selectBatchIds(Arrays.asList(ids));
        for (ProductSaleProduct product : products) {
            if (product == null) {
                continue;
            }
            assertDisabledBeforeDelete(product.getStatus());
            assertNoReferences(checkReferences(product.getSaleProductId()));
        }
        for (ProductSaleProduct product : products) {
            if (product != null) {
                deleteEmptySettings(product.getSaleProductId());
                recordChange(product.getSaleProductId(), product.getSaleProductCode(), "DELETE", product, null);
            }
        }
        return remove(saleProductMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        String normalizedStatus = normalizeStatus(status);
        ProductSaleProduct current = requireSaleProduct(id);
        if (STATUS_ENABLED.equals(normalizedStatus) && !PRICE_READY.equals(current.getPriceStatus())) {
            throw ServiceException.ofMessageKey("product.saleProduct.priceNotReady");
        }
        boolean updated = saleProductMapper.update(null, new LambdaUpdateWrapper<ProductSaleProduct>()
            .eq(ProductSaleProduct::getSaleProductId, id)
            .set(ProductSaleProduct::getStatus, normalizedStatus)) > 0;
        if (updated) {
            ProductSaleProduct after = ProductSaleProductEntityFactory.copyStatus(current);
            after.setStatus(normalizedStatus);
            recordChange(id, current.getSaleProductCode(), STATUS_ENABLED.equals(normalizedStatus) ? "ENABLE" : "DISABLE", current, after);
        }
        return updated;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        List<ProductPriceSetting> settings = settingMapper.selectList(activeQuery(ProductPriceSetting.class).eq("sale_product_id", id));
        List<Long> settingIds = settings.stream()
            .map(ProductPriceSetting::getPriceSettingId)
            .filter(Objects::nonNull)
            .toList();
        long count = 0;
        if (!settingIds.isEmpty()) {
            count += fabricMapper.selectCount(activeQuery(ProductPriceFabric.class).in("price_setting_id", settingIds));
            count += fabricRuleMapper.selectCount(activeQuery(ProductPriceFabricRule.class).in("price_setting_id", settingIds));
        }
        return referenceResult(count, "product.saleProduct.priceRuleReferenced", "Price rules: " + count);
    }

    private QueryWrapper<ProductSaleProduct> buildQueryWrapper(ProductSaleProductBo bo) {
        QueryWrapper<ProductSaleProduct> q = activeQuery(ProductSaleProduct.class);
        if (bo != null) {
            like(q, "sale_product_code", bo.getSaleProductCode());
            like(q, "sale_product_name", bo.getSaleProductName());
            eq(q, "category_id", bo.getCategoryId());
            eq(q, "category_code", bo.getCategoryCode());
            eq(q, "formula_id", bo.getFormulaId());
            eq(q, "formula_version_id", bo.getFormulaVersionId());
            eq(q, "price_status", bo.getPriceStatus());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private ProductFormula normalizeByFormula(ProductSaleProductBo bo) {
        if (bo == null || bo.getFormulaId() == null) {
            throw ServiceException.ofMessageKey("product.saleProduct.formulaRequired");
        }
        normalizeRequiredFields(bo);
        ProductFormula formula = formulaMapper.selectById(bo.getFormulaId());
        if (formula == null || !"0".equals(StringUtils.blankToDefault(formula.getDelFlag(), "0"))
            || !STATUS_EFFECTIVE.equals(formula.getStatus()) || formula.getCurrentVersionId() == null) {
            throw ServiceException.ofMessageKey("product.saleProduct.formulaEffectiveRequired");
        }
        ProductFormulaVersion version = versionMapper.selectById(formula.getCurrentVersionId());
        if (version == null || !STATUS_EFFECTIVE.equals(version.getVersionStatus())) {
            throw ServiceException.ofMessageKey("product.saleProduct.formulaEffectiveRequired");
        }
        bo.setFormulaVersionId(version.getVersionId());
        bo.setFormulaVersionNo(version.getVersionNo());
        bo.setFormulaVersionLabel(version.getVersionLabel());
        bo.setFormulaCode(formula.getFormulaCode());
        bo.setFormulaName(formula.getFormulaName());
        bo.setCategoryId(formula.getCategoryId());
        bo.setCategoryCode(formula.getCategoryCode());
        bo.setCategoryNameCn(formula.getCategoryNameCn());
        bo.setProductTypeCode(formula.getProductTypeCode());
        bo.setProductTypeNameCn(formula.getProductTypeNameCn());
        bo.setMinWidthInch(formula.getMinWidthInch());
        bo.setMinHeightInch(formula.getMinHeightInch());
        bo.setMaxWidthInch(formula.getMaxWidthInch());
        bo.setMaxHeightInch(formula.getMaxHeightInch());
        bo.setSizeSummary(formula.getSizeSummary());
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(STATUS_DISABLED);
        } else {
            bo.setStatus(normalizeStatus(bo.getStatus()));
        }
        if (StringUtils.isBlank(bo.getPriceStatus())) {
            bo.setPriceStatus(PRICE_NOT_READY);
        }
        return formula;
    }

    private void validateUnique(ProductSaleProductBo bo) {
        long codeCount = saleProductMapper.selectCount(activeQuery(ProductSaleProduct.class)
            .eq("sale_product_code", bo.getSaleProductCode())
            .ne(bo.getSaleProductId() != null, "sale_product_id", bo.getSaleProductId()));
        if (codeCount > 0) {
            throw ServiceException.ofMessageKey("product.saleProduct.codeExists");
        }
        long naturalCount = saleProductMapper.selectCount(activeQuery(ProductSaleProduct.class)
            .eq("sale_product_name", bo.getSaleProductName())
            .eq("category_id", bo.getCategoryId())
            .eq("formula_version_id", bo.getFormulaVersionId())
            .ne(bo.getSaleProductId() != null, "sale_product_id", bo.getSaleProductId()));
        if (naturalCount > 0) {
            throw ServiceException.ofMessageKey("product.saleProduct.naturalExists");
        }
    }

    private ProductSaleProduct requireSaleProduct(Long id) {
        ProductSaleProduct product = id == null ? null : saleProductMapper.selectById(id);
        if (product == null || !"0".equals(StringUtils.blankToDefault(product.getDelFlag(), "0"))) {
            throw ServiceException.ofMessageKey("product.saleProduct.notFound");
        }
        return product;
    }

    private void assertEditable(ProductSaleProduct product) {
        if (STATUS_ENABLED.equals(product.getStatus())) {
            throw ServiceException.ofMessageKey("product.saleProduct.enabledEditDenied");
        }
    }

    private ProductPriceSetting ensurePriceSetting(ProductSaleProduct product, ProductFormula formula) {
        ProductPriceSetting setting = settingMapper.selectOne(activeQuery(ProductPriceSetting.class)
            .eq("sale_product_id", product.getSaleProductId())
            .eq("formula_version_id", product.getFormulaVersionId())
            .last("limit 1"));
        if (setting == null) {
            setting = new ProductPriceSetting();
            setting.setStatus("DRAFT");
            setting.setValidationStatus(PRICE_NOT_READY);
            setting.setCurrencyCode("USD");
            ProductEntityDefaults.prepareInsert(setting);
        }
        setting.setTenantId(product.getTenantId() == null ? formula.getTenantId() : product.getTenantId());
        setting.setSaleProductId(product.getSaleProductId());
        setting.setSaleProductCode(product.getSaleProductCode());
        setting.setSaleProductName(product.getSaleProductName());
        setting.setFormulaId(product.getFormulaId());
        setting.setFormulaVersionId(product.getFormulaVersionId());
        setting.setFormulaVersionLabel(product.getFormulaVersionLabel());
        if (setting.getPriceSettingId() == null) {
            settingMapper.insert(setting);
        } else {
            settingMapper.updateById(setting);
        }
        return setting;
    }

    private void deleteEmptySettings(Long saleProductId) {
        settingMapper.delete(activeQuery(ProductPriceSetting.class).eq("sale_product_id", saleProductId));
    }

    private void normalizeRequiredFields(ProductSaleProductBo bo) {
        bo.setSaleProductCode(requiredTrim(bo.getSaleProductCode(), "product.saleProduct.codeRequired"));
        bo.setSaleProductName(requiredTrim(bo.getSaleProductName(), "product.saleProduct.nameRequired"));
    }

    private String requiredTrim(String value, String messageKey) {
        String trimmed = StringUtils.isBlank(value) ? null : value.trim();
        if (StringUtils.isBlank(trimmed)) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return trimmed;
    }

    private String normalizeStatus(String status) {
        String normalized = StringUtils.isBlank(status) ? null : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUS_ENABLED.equals(normalized) && !STATUS_DISABLED.equals(normalized)) {
            throw ServiceException.ofMessageKey("product.saleProduct.statusInvalid");
        }
        return normalized;
    }

    private void recordChange(Long id, String code, String action, Object before, Object after) {
        changeLogService.record("PRODUCT_PRICING", "SALE_PRODUCT", id, code, action, before, after, null);
    }
}
