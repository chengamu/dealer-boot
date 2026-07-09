package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.context.TenantContextHolder;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.bo.ProductShippingTemplateRuleBo;
import com.bocoo.product.domain.entity.ProductPriceFeeRule;
import com.bocoo.product.domain.entity.ProductShippingTemplate;
import com.bocoo.product.domain.entity.ProductShippingTemplateRule;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import com.bocoo.product.mapper.ProductPriceFeeRuleMapper;
import com.bocoo.product.mapper.ProductShippingTemplateMapper;
import com.bocoo.product.mapper.ProductShippingTemplateRuleMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductShippingTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductShippingTemplateServiceImpl extends ProductServiceSupport implements ProductShippingTemplateService {

    private final ProductShippingTemplateMapper templateMapper;
    private final ProductShippingTemplateRuleMapper ruleMapper;
    private final ProductPriceFeeRuleMapper feeRuleMapper;

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
    public ProductShippingTemplateVo queryDefaultEnabled() {
        ProductShippingTemplate template = templateMapper.selectOne(activeQuery(ProductShippingTemplate.class)
            .eq("status", STATUS_ENABLED).eq("default_flag", true).orderByAsc("sort_order").last("limit 1"));
        return template == null ? null : queryById(template.getShippingTemplateId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertByBo(ProductShippingTemplateBo bo) {
        normalize(bo);
        validateCodeUnique(bo);
        validateRules(bo.getRules());
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
        normalize(bo);
        validateCodeUnique(bo);
        validateRules(bo.getRules());
        ProductShippingTemplate current = templateMapper.selectById(bo.getShippingTemplateId());
        if (current == null) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.notFound");
        }
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
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            if (feeRuleMapper.selectCount(activeQuery(ProductPriceFeeRule.class).eq("shipping_template_id", id)) > 0) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.hasPriceReferences");
            }
        }
        return remove(templateMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        String normalizedStatus = normalizeStatus(status);
        ProductShippingTemplateVo current = queryById(id);
        if (current == null) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.notFound");
        }
        if (STATUS_ENABLED.equals(normalizedStatus)) {
            validateRules(current.getRules().stream().map(this::toBo).toList());
        }
        return templateMapper.update(null, new LambdaUpdateWrapper<ProductShippingTemplate>()
            .eq(ProductShippingTemplate::getShippingTemplateId, id)
            .set(ProductShippingTemplate::getStatus, normalizedStatus)) > 0;
    }

    private QueryWrapper<ProductShippingTemplate> buildQueryWrapper(ProductShippingTemplateBo bo) {
        QueryWrapper<ProductShippingTemplate> q = activeQuery(ProductShippingTemplate.class);
        if (bo != null) {
            like(q, "template_code", bo.getTemplateCode());
            like(q, "template_name", bo.getTemplateName());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalize(ProductShippingTemplateBo bo) {
        if (bo == null) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.notFound");
        }
        bo.setTemplateCode(trimToNull(bo.getTemplateCode()));
        bo.setTemplateName(trimToNull(bo.getTemplateName()));
        bo.setCurrencyCode(StringUtils.blankToDefault(trimToNull(bo.getCurrencyCode()), "USD"));
        bo.setStatus(normalizeStatus(StringUtils.blankToDefault(trimToNull(bo.getStatus()), STATUS_ENABLED)));
        bo.setTenantId(TenantContextHolder.getRequiredTenantId());
        if (StringUtils.isBlank(bo.getTemplateCode())) {
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

    private void validateRules(List<ProductShippingTemplateRuleBo> rules) {
        List<ProductShippingTemplateRuleBo> rows = rules == null ? List.of() : rules;
        Set<String> supportedCodes = Set.of(
            ProductPriceShippingRuleFactory.CODE_MANUAL,
            ProductPriceShippingRuleFactory.CODE_MOTORIZED
        );
        if (rows.stream().anyMatch(rule -> !supportedCodes.contains(rule.getFeeCode()))) {
            throw ServiceException.ofMessageKey("product.shippingTemplate.scenarioInvalid");
        }
        for (String code : List.of(ProductPriceShippingRuleFactory.CODE_MANUAL, ProductPriceShippingRuleFactory.CODE_MOTORIZED)) {
            if (rows.stream().noneMatch(rule -> code.equals(rule.getFeeCode()))) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.ruleRequired");
            }
            validateRanges(rows.stream().filter(rule -> code.equals(rule.getFeeCode())).toList());
        }
        for (ProductShippingTemplateRuleBo rule : rows) {
            if (!ProductPriceExpressionValidator.isShippingFormulaValid(rule.getFormulaText())) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.formulaInvalid");
            }
        }
    }

    private void validateRanges(List<ProductShippingTemplateRuleBo> rules) {
        BigDecimal previousMax = null;
        boolean unbounded = false;
        for (ProductShippingTemplateRuleBo rule : rules.stream()
            .sorted(Comparator.comparing(row -> row.getMinAreaSqft() == null ? BigDecimal.ZERO : row.getMinAreaSqft()))
            .toList()) {
            BigDecimal min = rule.getMinAreaSqft() == null ? BigDecimal.ZERO : rule.getMinAreaSqft();
            BigDecimal max = rule.getMaxAreaSqft();
            if (max != null && max.compareTo(min) <= 0) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.areaRangeInvalid");
            }
            if (unbounded || previousMax != null && min.compareTo(previousMax) < 0) {
                throw ServiceException.ofMessageKey("product.shippingTemplate.areaRangeOverlap");
            }
            if (max != null) {
                previousMax = max;
            } else {
                unbounded = true;
            }
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
        bo.setFormulaText(vo.getFormulaText());
        return bo;
    }

    private String defaultFeeName(String code) {
        return ProductPriceShippingRuleFactory.CODE_MOTORIZED.equals(code) ? "带电邮费" : "不带电邮费";
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
