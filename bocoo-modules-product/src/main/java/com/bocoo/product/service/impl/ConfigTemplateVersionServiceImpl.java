package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigTemplateVersionBo;
import com.bocoo.product.domain.entity.ConfigOption;
import com.bocoo.product.domain.entity.ConfigQuestion;
import com.bocoo.product.domain.entity.ConfigRule;
import com.bocoo.product.domain.entity.ConfigTemplateVersion;
import com.bocoo.product.domain.entity.SalesProduct;
import com.bocoo.product.domain.vo.ConfigTemplateVersionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ConfigOptionMapper;
import com.bocoo.product.mapper.ConfigQuestionMapper;
import com.bocoo.product.mapper.ConfigRuleMapper;
import com.bocoo.product.mapper.ConfigTemplateVersionMapper;
import com.bocoo.product.mapper.SalesProductMapper;
import com.bocoo.product.service.ConfigTemplateVersionService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigTemplateVersionServiceImpl extends ProductServiceSupport implements ConfigTemplateVersionService {

    private final ConfigTemplateVersionMapper configTemplateVersionMapper;
    private final ConfigQuestionMapper questionMapper;

    private final ConfigOptionMapper optionMapper;

    private final ConfigRuleMapper ruleMapper;

    private final SalesProductMapper salesProductMapper;

    @Override
    public TableDataInfo<ConfigTemplateVersionVo> queryPageList(ConfigTemplateVersionBo bo, PageQuery pageQuery) {
        return page(configTemplateVersionMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ConfigTemplateVersionVo> queryList(ConfigTemplateVersionBo bo) {
        return configTemplateVersionMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ConfigTemplateVersionVo queryById(Long id) {
        return configTemplateVersionMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ConfigTemplateVersionBo bo) {
        ConfigTemplateVersion entity = MapstructUtils.convert(bo, ConfigTemplateVersion.class);
        if (entity == null) { return Boolean.FALSE; }
        ProductEntityDefaults.prepareInsert(entity);
        return configTemplateVersionMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ConfigTemplateVersionBo bo) {
        ConfigTemplateVersion entity = MapstructUtils.convert(bo, ConfigTemplateVersion.class);
        if (entity == null) { return Boolean.FALSE; }
        return configTemplateVersionMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(configTemplateVersionMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return configTemplateVersionMapper.update(null, new LambdaUpdateWrapper<ConfigTemplateVersion>()
            .eq(ConfigTemplateVersion::getTemplateVersionId, id)
            .set(ConfigTemplateVersion::getVersionStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        long count = questionMapper.selectCount(activeQuery(ConfigQuestion.class).eq("template_version_id", id))
            + optionMapper.selectCount(activeQuery(ConfigOption.class).eq("template_version_id", id))
            + ruleMapper.selectCount(activeQuery(ConfigRule.class).eq("template_version_id", id))
            + salesProductMapper.selectCount(activeQuery(SalesProduct.class).eq("template_version_id", id));
        return referenceResult(count, "product.configTemplateVersion.hasReferences", "Questions / options / rules / sales products: " + count);
    }

    private QueryWrapper<ConfigTemplateVersion> buildQueryWrapper(ConfigTemplateVersionBo bo) {
        QueryWrapper<ConfigTemplateVersion> q = activeQuery(ConfigTemplateVersion.class);
        if (bo != null) {
        like(q, "template_code", bo.getTemplateCode());
        like(q, "version_no", bo.getVersionNo());
        like(q, "version_status", bo.getVersionStatus());
        like(q, "product_model_code", bo.getProductModelCode());
        eq(q, "sales_product_id", bo.getSalesProductId());
        like(q, "sales_product_code", bo.getSalesProductCode());
        like(q, "sales_variant_code", bo.getSalesVariantCode());
        like(q, "price_plan_code", bo.getPricePlanCode());
        }
        return q.orderByDesc("update_time");
    }

}
