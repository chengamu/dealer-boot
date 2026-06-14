package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.*;
import com.bocoo.product.domain.entity.*;
import com.bocoo.product.domain.vo.*;
import com.bocoo.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 产品配置模板服务。
 */
@Service
@RequiredArgsConstructor
public class ProductConfigService extends ProductCrudSupport {

    private final SalesProductMapper salesProductMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ConfigTemplateMapper templateIdMapper;
    private final ConfigTemplateVersionMapper templateVersionIdMapper;
    private final QuestionGroupMapper questionGroupIdMapper;
    private final ConfigQuestionMapper questionIdMapper;
    private final ConfigOptionMapper optionIdMapper;
    private final ConfigRuleMapper ruleIdMapper;
    private final ConfigEvaluationEngine configEvaluationEngine;

    public TableDataInfo<SalesProductVo> querySalesProductPage(SalesProductBo bo, PageQuery pageQuery) {
        return page(salesProductMapper, pageQuery, buildSalesProductWrapper(bo));
    }

    public java.util.List<SalesProductVo> querySalesProductList(SalesProductBo bo) {
        return salesProductMapper.selectVoList(buildSalesProductWrapper(bo));
    }

    public SalesProductVo getSalesProductById(Long id) {
        return salesProductMapper.selectVoById(id);
    }

    public Boolean saveSalesProduct(SalesProductBo bo) {
        SalesProduct entity = MapstructUtils.convert(bo, SalesProduct.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        syncSalesProductCategorySnapshot(entity);
        if (entity.getSalesProductId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return salesProductMapper.insert(entity) > 0;
        }
        return salesProductMapper.updateById(entity) > 0;
    }

    public Boolean removeSalesProductByIds(Long[] ids) {
        return remove(salesProductMapper, ids);
    }

    public Boolean updateSalesProductStatus(Long id, String status) {
        return updateStatus(salesProductMapper, SalesProduct.class, "salesProductId", id, status);
    }

    public ReferenceCheckResultVo checkSalesProductReferences(Long id) {
        SalesProduct product = salesProductMapper.selectById(id);
        if (product == null) {
            return referenceResult(0, null, null);
        }
        long count = templateIdMapper.selectCount(activeQuery(ConfigTemplate.class).eq("sales_product_id", id))
            + templateVersionIdMapper.selectCount(activeQuery(ConfigTemplateVersion.class).eq("sales_product_id", id));
        return referenceResult(count, "product.salesProduct.hasReferences", "Config templates / versions: " + count);
    }

    private QueryWrapper<SalesProduct> buildSalesProductWrapper(SalesProductBo bo) {
        QueryWrapper<SalesProduct> q = activeQuery(SalesProduct.class);
        if (bo != null) {
            like(q, "sales_product_code", bo.getSalesProductCode());
            if (StringUtils.isNotBlank(bo.getSalesProductNameCn())) {
                q.and(wrapper -> wrapper.like("sales_product_name_cn", bo.getSalesProductNameCn()).or().like("sales_product_name_en", bo.getSalesProductNameCn()));
            }
            like(q, "sales_product_name_en", bo.getSalesProductNameEn());
            eq(q, "category_id", bo.getCategoryId());
            eq(q, "category_code", bo.getCategoryCode());
            eq(q, "product_type", bo.getProductType());
            eq(q, "sales_mode", bo.getSalesMode());
            eq(q, "template_code", bo.getTemplateCode());
            eq(q, "biz_status", bo.getBizStatus());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "sales_product_id");
    }

    private void syncSalesProductCategorySnapshot(SalesProduct product) {
        if (product.getCategoryId() == null) {
            return;
        }
        ProductCategory category = categoryMapper.selectById(product.getCategoryId());
        if (category == null) {
            return;
        }
        product.setCategoryCode(category.getCategoryCode());
        product.setCategoryNameCn(category.getCategoryNameCn());
        product.setCategoryNameEn(category.getCategoryNameEn());
    }

    public TableDataInfo<ConfigTemplateVo> queryConfigTemplatePage(ConfigTemplateBo bo, PageQuery pageQuery) {
        Page<ConfigTemplateVo> result = templateIdMapper.selectVoPage(pageQuery.build(), buildConfigTemplateWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<ConfigTemplateVo> queryConfigTemplateList(ConfigTemplateBo bo) {
        return templateIdMapper.selectVoList(buildConfigTemplateWrapper(bo));
    }

    public ConfigTemplateVo getConfigTemplateById(Long id) {
        return templateIdMapper.selectVoById(id);
    }

    public Boolean saveConfigTemplate(ConfigTemplateBo bo) {
        ConfigTemplate entity = MapstructUtils.convert(bo, ConfigTemplate.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getTemplateId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return templateIdMapper.insert(entity) > 0;
        }
        return templateIdMapper.updateById(entity) > 0;
    }

    public Boolean removeConfigTemplateByIds(Long[] ids) {
        return templateIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateConfigTemplateStatus(Long id, String status) {
        return updateStatus(templateIdMapper, ConfigTemplate.class, "templateId", id, status);
    }

    public ReferenceCheckResultVo checkConfigTemplateReferences(Long id) {
        long count = templateVersionIdMapper.selectCount(activeQuery(ConfigTemplateVersion.class).eq("template_id", id))
            + salesProductMapper.selectCount(activeQuery(SalesProduct.class).eq("template_id", id));
        return referenceResult(count, "product.configTemplate.hasReferences", "Template versions / sales products: " + count);
    }

    private LambdaQueryWrapper<ConfigTemplate> buildConfigTemplateWrapper(ConfigTemplateBo bo) {
        if (bo == null) {
            bo = new ConfigTemplateBo();
        }
        LambdaQueryWrapper<ConfigTemplate> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTemplateCode()), ConfigTemplate::getTemplateCode, bo.getTemplateCode());
        lqw.like(StringUtils.isNotBlank(bo.getTemplateNameCn()), ConfigTemplate::getTemplateNameCn, bo.getTemplateNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getTemplateNameEn()), ConfigTemplate::getTemplateNameEn, bo.getTemplateNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getProductModelCode()), ConfigTemplate::getProductModelCode, bo.getProductModelCode());
        lqw.eq(bo.getSalesProductId() != null, ConfigTemplate::getSalesProductId, bo.getSalesProductId());
        lqw.like(StringUtils.isNotBlank(bo.getSalesProductCode()), ConfigTemplate::getSalesProductCode, bo.getSalesProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getBizStatus()), ConfigTemplate::getBizStatus, bo.getBizStatus());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ConfigTemplate::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), ConfigTemplate::getDelFlag, "0");
        return lqw;
    }


    public TableDataInfo<ConfigTemplateVersionVo> queryConfigTemplateVersionPage(ConfigTemplateVersionBo bo, PageQuery pageQuery) {
        Page<ConfigTemplateVersionVo> result = templateVersionIdMapper.selectVoPage(pageQuery.build(), buildConfigTemplateVersionWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<ConfigTemplateVersionVo> queryConfigTemplateVersionList(ConfigTemplateVersionBo bo) {
        return templateVersionIdMapper.selectVoList(buildConfigTemplateVersionWrapper(bo));
    }

    public ConfigTemplateVersionVo getConfigTemplateVersionById(Long id) {
        return templateVersionIdMapper.selectVoById(id);
    }

    public Boolean saveConfigTemplateVersion(ConfigTemplateVersionBo bo) {
        ConfigTemplateVersion entity = MapstructUtils.convert(bo, ConfigTemplateVersion.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getTemplateVersionId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return templateVersionIdMapper.insert(entity) > 0;
        }
        return templateVersionIdMapper.updateById(entity) > 0;
    }

    public Boolean removeConfigTemplateVersionByIds(Long[] ids) {
        return templateVersionIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateConfigTemplateVersionStatus(Long id, String status) {
        ConfigTemplateVersion entity = new ConfigTemplateVersion();
        entity.setTemplateVersionId(id);
        entity.setVersionStatus(status);
        return templateVersionIdMapper.updateById(entity) > 0;
    }

    public ReferenceCheckResultVo checkConfigTemplateVersionReferences(Long id) {
        long count = questionIdMapper.selectCount(activeQuery(ConfigQuestion.class).eq("template_version_id", id))
            + optionIdMapper.selectCount(activeQuery(ConfigOption.class).eq("template_version_id", id))
            + ruleIdMapper.selectCount(activeQuery(ConfigRule.class).eq("template_version_id", id))
            + salesProductMapper.selectCount(activeQuery(SalesProduct.class).eq("template_version_id", id));
        return referenceResult(count, "product.configTemplateVersion.hasReferences", "Questions / options / rules / sales products: " + count);
    }

    private LambdaQueryWrapper<ConfigTemplateVersion> buildConfigTemplateVersionWrapper(ConfigTemplateVersionBo bo) {
        if (bo == null) {
            bo = new ConfigTemplateVersionBo();
        }
        LambdaQueryWrapper<ConfigTemplateVersion> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTemplateCode()), ConfigTemplateVersion::getTemplateCode, bo.getTemplateCode());
        lqw.like(StringUtils.isNotBlank(bo.getVersionNo()), ConfigTemplateVersion::getVersionNo, bo.getVersionNo());
        lqw.like(StringUtils.isNotBlank(bo.getVersionStatus()), ConfigTemplateVersion::getVersionStatus, bo.getVersionStatus());
        lqw.like(StringUtils.isNotBlank(bo.getProductModelCode()), ConfigTemplateVersion::getProductModelCode, bo.getProductModelCode());
        lqw.eq(bo.getSalesProductId() != null, ConfigTemplateVersion::getSalesProductId, bo.getSalesProductId());
        lqw.like(StringUtils.isNotBlank(bo.getSalesProductCode()), ConfigTemplateVersion::getSalesProductCode, bo.getSalesProductCode());
        lqw.like(StringUtils.isNotBlank(bo.getSalesVariantCode()), ConfigTemplateVersion::getSalesVariantCode, bo.getSalesVariantCode());
        lqw.like(StringUtils.isNotBlank(bo.getPricePlanCode()), ConfigTemplateVersion::getPricePlanCode, bo.getPricePlanCode());
        return lqw;
    }


    public TableDataInfo<QuestionGroupVo> queryQuestionGroupPage(QuestionGroupBo bo, PageQuery pageQuery) {
        Page<QuestionGroupVo> result = questionGroupIdMapper.selectVoPage(pageQuery.build(), buildQuestionGroupWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<QuestionGroupVo> queryQuestionGroupList(QuestionGroupBo bo) {
        return questionGroupIdMapper.selectVoList(buildQuestionGroupWrapper(bo));
    }

    public QuestionGroupVo getQuestionGroupById(Long id) {
        return questionGroupIdMapper.selectVoById(id);
    }

    public Boolean saveQuestionGroup(QuestionGroupBo bo) {
        QuestionGroup entity = MapstructUtils.convert(bo, QuestionGroup.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getQuestionGroupId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return questionGroupIdMapper.insert(entity) > 0;
        }
        return questionGroupIdMapper.updateById(entity) > 0;
    }

    public Boolean removeQuestionGroupByIds(Long[] ids) {
        return questionGroupIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateQuestionGroupStatus(Long id, String status) {
        return updateStatus(questionGroupIdMapper, QuestionGroup.class, "questionGroupId", id, status);
    }

    public ReferenceCheckResultVo checkQuestionGroupReferences(Long id) {
        long count = questionIdMapper.selectCount(activeQuery(ConfigQuestion.class).eq("question_group_id", id));
        return referenceResult(count, "product.questionGroup.hasReferences", "Config questions: " + count);
    }

    private LambdaQueryWrapper<QuestionGroup> buildQuestionGroupWrapper(QuestionGroupBo bo) {
        if (bo == null) {
            bo = new QuestionGroupBo();
        }
        LambdaQueryWrapper<QuestionGroup> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getGroupCode()), QuestionGroup::getGroupCode, bo.getGroupCode());
        lqw.like(StringUtils.isNotBlank(bo.getGroupNameCn()), QuestionGroup::getGroupNameCn, bo.getGroupNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getGroupNameEn()), QuestionGroup::getGroupNameEn, bo.getGroupNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), QuestionGroup::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), QuestionGroup::getDelFlag, "0");
        return lqw;
    }


    public TableDataInfo<ConfigQuestionVo> queryConfigQuestionPage(ConfigQuestionBo bo, PageQuery pageQuery) {
        Page<ConfigQuestionVo> result = questionIdMapper.selectVoPage(pageQuery.build(), buildConfigQuestionWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<ConfigQuestionVo> queryConfigQuestionList(ConfigQuestionBo bo) {
        return questionIdMapper.selectVoList(buildConfigQuestionWrapper(bo));
    }

    public ConfigQuestionVo getConfigQuestionById(Long id) {
        return questionIdMapper.selectVoById(id);
    }

    public Boolean saveConfigQuestion(ConfigQuestionBo bo) {
        ConfigQuestion entity = MapstructUtils.convert(bo, ConfigQuestion.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getQuestionId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return questionIdMapper.insert(entity) > 0;
        }
        return questionIdMapper.updateById(entity) > 0;
    }

    public Boolean removeConfigQuestionByIds(Long[] ids) {
        return questionIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateConfigQuestionStatus(Long id, String status) {
        return updateStatus(questionIdMapper, ConfigQuestion.class, "questionId", id, status);
    }

    public ReferenceCheckResultVo checkConfigQuestionReferences(Long id) {
        long count = optionIdMapper.selectCount(activeQuery(ConfigOption.class).eq("question_id", id));
        return referenceResult(count, "product.configQuestion.hasReferences", "Config options: " + count);
    }

    private LambdaQueryWrapper<ConfigQuestion> buildConfigQuestionWrapper(ConfigQuestionBo bo) {
        if (bo == null) {
            bo = new ConfigQuestionBo();
        }
        LambdaQueryWrapper<ConfigQuestion> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getTemplateVersionId() != null, ConfigQuestion::getTemplateVersionId, bo.getTemplateVersionId());
        lqw.eq(bo.getQuestionGroupId() != null, ConfigQuestion::getQuestionGroupId, bo.getQuestionGroupId());
        lqw.like(StringUtils.isNotBlank(bo.getQuestionCode()), ConfigQuestion::getQuestionCode, bo.getQuestionCode());
        lqw.like(StringUtils.isNotBlank(bo.getQuestionNameCn()), ConfigQuestion::getQuestionNameCn, bo.getQuestionNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getQuestionNameEn()), ConfigQuestion::getQuestionNameEn, bo.getQuestionNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getInputType()), ConfigQuestion::getInputType, bo.getInputType());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ConfigQuestion::getStatus, bo.getStatus());
        return lqw;
    }


    public TableDataInfo<ConfigOptionVo> queryConfigOptionPage(ConfigOptionBo bo, PageQuery pageQuery) {
        Page<ConfigOptionVo> result = optionIdMapper.selectVoPage(pageQuery.build(), buildConfigOptionWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<ConfigOptionVo> queryConfigOptionList(ConfigOptionBo bo) {
        return optionIdMapper.selectVoList(buildConfigOptionWrapper(bo));
    }

    public ConfigOptionVo getConfigOptionById(Long id) {
        return optionIdMapper.selectVoById(id);
    }

    public Boolean saveConfigOption(ConfigOptionBo bo) {
        ConfigOption entity = MapstructUtils.convert(bo, ConfigOption.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getOptionId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return optionIdMapper.insert(entity) > 0;
        }
        return optionIdMapper.updateById(entity) > 0;
    }

    public Boolean removeConfigOptionByIds(Long[] ids) {
        return optionIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateConfigOptionStatus(Long id, String status) {
        return updateStatus(optionIdMapper, ConfigOption.class, "optionId", id, status);
    }

    public ReferenceCheckResultVo checkConfigOptionReferences(Long id) {
        ConfigOption option = optionIdMapper.selectById(id);
        if (option == null || StringUtils.isBlank(option.getOptionCode())) {
            return referenceResult(0, null, null);
        }
        long count = ruleIdMapper.selectCount(activeQuery(ConfigRule.class).like("condition_json", option.getOptionCode()))
            + ruleIdMapper.selectCount(activeQuery(ConfigRule.class).like("action_json", option.getOptionCode()));
        return referenceResult(count, "product.configOption.hasReferences", "Config rules mentioning option: " + count);
    }

    private LambdaQueryWrapper<ConfigOption> buildConfigOptionWrapper(ConfigOptionBo bo) {
        if (bo == null) {
            bo = new ConfigOptionBo();
        }
        LambdaQueryWrapper<ConfigOption> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getQuestionId() != null, ConfigOption::getQuestionId, bo.getQuestionId());
        lqw.eq(bo.getTemplateVersionId() != null, ConfigOption::getTemplateVersionId, bo.getTemplateVersionId());
        lqw.like(StringUtils.isNotBlank(bo.getOptionCode()), ConfigOption::getOptionCode, bo.getOptionCode());
        lqw.like(StringUtils.isNotBlank(bo.getOptionNameCn()), ConfigOption::getOptionNameCn, bo.getOptionNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getOptionNameEn()), ConfigOption::getOptionNameEn, bo.getOptionNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ConfigOption::getStatus, bo.getStatus());
        return lqw;
    }


    public TableDataInfo<ConfigRuleVo> queryConfigRulePage(ConfigRuleBo bo, PageQuery pageQuery) {
        Page<ConfigRuleVo> result = ruleIdMapper.selectVoPage(pageQuery.build(), buildConfigRuleWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<ConfigRuleVo> queryConfigRuleList(ConfigRuleBo bo) {
        return ruleIdMapper.selectVoList(buildConfigRuleWrapper(bo));
    }

    public ConfigRuleVo getConfigRuleById(Long id) {
        return ruleIdMapper.selectVoById(id);
    }

    public Boolean saveConfigRule(ConfigRuleBo bo) {
        ConfigRule entity = MapstructUtils.convert(bo, ConfigRule.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getRuleId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return ruleIdMapper.insert(entity) > 0;
        }
        return ruleIdMapper.updateById(entity) > 0;
    }

    public Boolean removeConfigRuleByIds(Long[] ids) {
        return ruleIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    public Boolean updateConfigRuleStatus(Long id, String status) {
        return updateStatus(ruleIdMapper, ConfigRule.class, "ruleId", id, status);
    }

    private LambdaQueryWrapper<ConfigRule> buildConfigRuleWrapper(ConfigRuleBo bo) {
        if (bo == null) {
            bo = new ConfigRuleBo();
        }
        LambdaQueryWrapper<ConfigRule> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getTemplateVersionId() != null, ConfigRule::getTemplateVersionId, bo.getTemplateVersionId());
        lqw.like(StringUtils.isNotBlank(bo.getRuleCode()), ConfigRule::getRuleCode, bo.getRuleCode());
        lqw.like(StringUtils.isNotBlank(bo.getRuleNameCn()), ConfigRule::getRuleNameCn, bo.getRuleNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getRuleNameEn()), ConfigRule::getRuleNameEn, bo.getRuleNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getRuleType()), ConfigRule::getRuleType, bo.getRuleType());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ConfigRule::getStatus, bo.getStatus());
        return lqw;
    }

    public ConfigEvaluationResultVo evaluate(ConfigEvaluationBo bo) {
        if (bo == null) {
            bo = new ConfigEvaluationBo();
        }
        ConfigQuestionBo questionBo = new ConfigQuestionBo();
        questionBo.setTemplateVersionId(bo.getTemplateVersionId());
        questionBo.setStatus("ENABLED");
        ConfigOptionBo optionBo = new ConfigOptionBo();
        optionBo.setTemplateVersionId(bo.getTemplateVersionId());
        optionBo.setStatus("ENABLED");
        return configEvaluationEngine.evaluate(bo, queryConfigQuestionList(questionBo), queryConfigOptionList(optionBo));
    }
}
