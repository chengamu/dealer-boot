package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigRuleBo;
import com.bocoo.product.domain.entity.ConfigRule;
import com.bocoo.product.domain.vo.ConfigRuleVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ConfigRuleMapper;
import com.bocoo.product.service.ConfigRuleService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigRuleServiceImpl extends ProductServiceSupport implements ConfigRuleService {

    private final ConfigRuleMapper configRuleMapper;

    @Override
    public TableDataInfo<ConfigRuleVo> queryPageList(ConfigRuleBo bo, PageQuery pageQuery) {
        return page(configRuleMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ConfigRuleVo> queryList(ConfigRuleBo bo) {
        return configRuleMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ConfigRuleVo queryById(Long id) {
        return configRuleMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ConfigRuleBo bo) {
        ConfigRule entity = MapstructUtils.convert(bo, ConfigRule.class);
        if (entity == null) { return Boolean.FALSE; }
        ProductEntityDefaults.prepareInsert(entity);
        return configRuleMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ConfigRuleBo bo) {
        ConfigRule entity = MapstructUtils.convert(bo, ConfigRule.class);
        if (entity == null) { return Boolean.FALSE; }
        return configRuleMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(configRuleMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return configRuleMapper.update(null, new LambdaUpdateWrapper<ConfigRule>()
            .eq(ConfigRule::getRuleId, id)
            .set(ConfigRule::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        return referenceResult(0, null, null);
    }

    private QueryWrapper<ConfigRule> buildQueryWrapper(ConfigRuleBo bo) {
        QueryWrapper<ConfigRule> q = activeQuery(ConfigRule.class);
        if (bo != null) {
        eq(q, "template_version_id", bo.getTemplateVersionId());
        like(q, "rule_code", bo.getRuleCode());
        like(q, "rule_name_cn", bo.getRuleNameCn());
        like(q, "rule_name_en", bo.getRuleNameEn());
        like(q, "rule_type", bo.getRuleType());
        like(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "rule_id");
    }

}
