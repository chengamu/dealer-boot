package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.lock.annotation.Lock4j;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.uuid.Seq;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.bo.ProductShippingTemplateRuleBo;
import com.bocoo.product.domain.entity.ProductShippingTemplate;
import com.bocoo.product.domain.entity.ProductShippingTemplateRule;
import com.bocoo.product.domain.vo.ProductShippingTemplateRuleImportVo;
import com.bocoo.product.domain.vo.ProductShippingTemplateRuleVo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import com.bocoo.product.mapper.ProductShippingTemplateMapper;
import com.bocoo.product.mapper.ProductShippingTemplateRuleMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductShippingTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductShippingTemplateServiceImpl extends ProductServiceSupport implements ProductShippingTemplateService {

    private final ProductShippingTemplateMapper templateMapper;
    private final ProductShippingTemplateRuleMapper ruleMapper;
    private final ProductShippingTemplateRuleValidator ruleValidator;
    private final ProductShippingTemplateRuleImporter ruleImporter;
    private final ProductQuoteReferenceGuard quoteReferenceGuard;

    @Override
    public TableDataInfo<ProductShippingTemplateVo> queryPageList(ProductShippingTemplateBo bo, PageQuery pageQuery) {
        TableDataInfo<ProductShippingTemplateVo> result =
            page(templateMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "shipping_template_id"));
        fillRuleCounts(result.getRows());
        return result;
    }

    @Override
    public List<ProductShippingTemplateVo> queryList(ProductShippingTemplateBo bo) {
        return templateMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo),
            q -> q.orderByAsc("sort_order", "shipping_template_id")));
    }

    @Override
    public ProductShippingTemplateVo queryById(Long id) {
        ProductShippingTemplateVo vo = templateMapper.selectVoById(id);
        if (vo != null) {
            vo.setRules(ruleMapper.selectVoList(activeQuery(ProductShippingTemplateRule.class)
                .eq("shipping_template_id", id).orderByAsc("sort_order", "shipping_template_rule_id")));
        }
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ProductShippingTemplateBo bo) {
        normalize(bo, true);
        validateCodeUnique(bo);
        ruleValidator.validate(bo.getRules());
        ProductShippingTemplate entity = MapstructUtils.convert(bo, ProductShippingTemplate.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        templateMapper.insert(entity);
        saveRules(entity, bo.getRules());
        clearOtherDefault(entity);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateByBo(ProductShippingTemplateBo bo) {
        ProductShippingTemplate current = templateMapper.selectById(bo.getShippingTemplateId());
        if (current == null) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.notFound");
        }
        if (STATUS_ENABLED.equals(current.getStatus())) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.enabledEditDenied");
        }
        bo.setTemplateCode(current.getTemplateCode());
        bo.setDefaultFlag(current.getDefaultFlag());
        bo.setSortOrder(current.getSortOrder());
        normalize(bo, false);
        validateCodeUnique(bo);
        ruleValidator.validate(bo.getRules());
        ProductShippingTemplate entity = MapstructUtils.convert(bo, ProductShippingTemplate.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        templateMapper.updateById(entity);
        ruleMapper.delete(activeQuery(ProductShippingTemplateRule.class)
            .eq("shipping_template_id", entity.getShippingTemplateId()));
        saveRules(entity, bo.getRules());
        clearOtherDefault(entity);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Long[] ids) {
        if (templateMapper.selectBatchIds(Arrays.asList(ids)).stream()
            .anyMatch(row -> STATUS_ENABLED.equals(row.getStatus()))) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.enabledDeleteDenied");
        }
        Arrays.stream(ids).forEach(quoteReferenceGuard::assertNoShippingTemplateReferences);
        ruleMapper.delete(activeQuery(ProductShippingTemplateRule.class)
            .in("shipping_template_id", Arrays.asList(ids)));
        return remove(templateMapper, ids);
    }

    @Override
    @Lock4j(keys = {"'shipping-template-status'"})
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateStatus(Long id, String status) {
        String normalizedStatus = normalizeStatus(status);
        ProductShippingTemplateVo current = queryById(id);
        if (current == null) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.notFound");
        }
        if (STATUS_ENABLED.equals(normalizedStatus)) {
            validateLegacyRulesMigrated(id);
            ruleValidator.validate(current.getRules().stream().map(this::toBo).toList());
            templateMapper.update(null, new LambdaUpdateWrapper<ProductShippingTemplate>()
                .eq(ProductShippingTemplate::getTenantId, current.getTenantId())
                .eq(ProductShippingTemplate::getCurrencyCode, current.getCurrencyCode())
                .eq(ProductShippingTemplate::getDelFlag, "0")
                .eq(ProductShippingTemplate::getStatus, STATUS_ENABLED)
                .ne(ProductShippingTemplate::getShippingTemplateId, id)
                .set(ProductShippingTemplate::getStatus, STATUS_DISABLED));
        }
        return templateMapper.update(null, new LambdaUpdateWrapper<ProductShippingTemplate>()
            .eq(ProductShippingTemplate::getShippingTemplateId, id)
            .set(ProductShippingTemplate::getStatus, normalizedStatus)) > 0;
    }

    private void validateLegacyRulesMigrated(Long templateId) {
        long legacyRuleCount = ruleMapper.selectCount(activeQuery(ProductShippingTemplateRule.class)
            .eq("shipping_template_id", templateId)
            .isNotNull("formula_text"));
        if (legacyRuleCount > 0) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.legacyFormulaMigrationRequired");
        }
    }

    private QueryWrapper<ProductShippingTemplate> buildQueryWrapper(ProductShippingTemplateBo bo) {
        QueryWrapper<ProductShippingTemplate> q = activeQuery(ProductShippingTemplate.class);
        if (bo != null) {
            like(q, "template_code", bo.getTemplateCode());
            like(q, "template_name", bo.getTemplateName());
            eq(q, "currency_code", bo.getCurrencyCode());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    @Override
    public List<ProductShippingTemplateRuleVo> importRules(InputStream inputStream) {
        List<ProductShippingTemplateRuleVo> rules = ruleImporter.importRules(inputStream);
        ruleValidator.validate(rules.stream().map(this::toBo).toList());
        return rules;
    }

    @Override
    public List<ProductShippingTemplateRuleImportVo> importTemplateRows() {
        return ruleImporter.templateRows();
    }

    private void normalize(ProductShippingTemplateBo bo, boolean insert) {
        if (bo == null) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.notFound");
        }
        bo.setTemplateCode(trimToNull(bo.getTemplateCode()));
        if (insert) {
            bo.setTemplateCode("SHIP-" + Seq.getId());
        }
        bo.setTemplateName(trimToNull(bo.getTemplateName()));
        bo.setCurrencyCode(StringUtils.blankToDefault(trimToNull(bo.getCurrencyCode()), "USD")
            .toUpperCase(Locale.ROOT));
        bo.setStatus(insert
            ? STATUS_DISABLED
            : normalizeStatus(StringUtils.blankToDefault(trimToNull(bo.getStatus()), STATUS_DISABLED)));
        bo.setDefaultFlag(Boolean.TRUE.equals(bo.getDefaultFlag()));
        bo.setSortOrder(bo.getSortOrder() == null ? 0 : bo.getSortOrder());
        bo.setTenantId(TenantContextHolder.getRequiredTenantId());
        if (!insert && StringUtils.isBlank(bo.getTemplateCode())) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.codeRequired");
        }
        if (StringUtils.isBlank(bo.getTemplateName())) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.nameRequired");
        }
    }

    private void validateCodeUnique(ProductShippingTemplateBo bo) {
        long count = templateMapper.selectCount(activeQuery(ProductShippingTemplate.class)
            .eq("template_code", bo.getTemplateCode())
            .ne(bo.getShippingTemplateId() != null, "shipping_template_id", bo.getShippingTemplateId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.codeExists");
        }
    }

    private void fillRuleCounts(List<ProductShippingTemplateVo> rows) {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        List<Long> templateIds = rows.stream().map(ProductShippingTemplateVo::getShippingTemplateId).toList();
        Map<Long, Long> counts = ruleMapper.selectList(activeQuery(ProductShippingTemplateRule.class)
                .in("shipping_template_id", templateIds)).stream()
            .collect(Collectors.groupingBy(ProductShippingTemplateRule::getShippingTemplateId, Collectors.counting()));
        rows.forEach(row -> row.setRuleCount(counts.getOrDefault(row.getShippingTemplateId(), 0L).intValue()));
    }

    private void saveRules(ProductShippingTemplate entity, List<ProductShippingTemplateRuleBo> rules) {
        int index = 0;
        for (ProductShippingTemplateRuleBo ruleBo : rules == null ? List.<ProductShippingTemplateRuleBo>of() : rules) {
            ProductShippingTemplateRule rule = MapstructUtils.convert(ruleBo, ProductShippingTemplateRule.class);
            if (rule == null) {
                continue;
            }
            rule.setTenantId(entity.getTenantId());
            rule.setShippingTemplateId(entity.getShippingTemplateId());
            rule.setFeeName(defaultFeeName(rule.getFeeCode()));
            rule.setSortOrder(rule.getSortOrder() == null ? index : rule.getSortOrder());
            ProductEntityDefaults.prepareInsert(rule);
            ruleMapper.insert(rule);
            index++;
        }
    }

    private void clearOtherDefault(ProductShippingTemplate entity) {
        if (Boolean.TRUE.equals(entity.getDefaultFlag())) {
            templateMapper.update(null, new LambdaUpdateWrapper<ProductShippingTemplate>()
                .ne(ProductShippingTemplate::getShippingTemplateId, entity.getShippingTemplateId())
                .eq(ProductShippingTemplate::getDelFlag, "0")
                .set(ProductShippingTemplate::getDefaultFlag, false));
        }
    }

    private ProductShippingTemplateRuleBo toBo(com.bocoo.product.domain.vo.ProductShippingTemplateRuleVo vo) {
        ProductShippingTemplateRuleBo bo = new ProductShippingTemplateRuleBo();
        bo.setFeeCode(vo.getFeeCode());
        bo.setMinAreaSqft(vo.getMinAreaSqft());
        bo.setMaxAreaSqft(vo.getMaxAreaSqft());
        bo.setFeeAmount(vo.getFeeAmount());
        return bo;
    }

    private String defaultFeeName(String code) {
        return ProductShippingRuleSupport.feeName(code);
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String normalizeStatus(String status) {
        String normalized = StringUtils.isBlank(status) ? null : status.trim().toUpperCase(Locale.ROOT);
        if (!STATUS_ENABLED.equals(normalized) && !STATUS_DISABLED.equals(normalized)) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.statusInvalid");
        }
        return normalized;
    }
}
