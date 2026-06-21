package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigOptionBo;
import com.bocoo.product.domain.entity.ConfigOption;
import com.bocoo.product.domain.entity.ConfigRule;
import com.bocoo.product.domain.vo.ConfigOptionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ConfigOptionMapper;
import com.bocoo.product.mapper.ConfigRuleMapper;
import com.bocoo.product.service.ConfigOptionService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigOptionServiceImpl extends ProductServiceSupport implements ConfigOptionService {

    private final ConfigOptionMapper configOptionMapper;
    private final ConfigRuleMapper ruleMapper;

    @Override
    public TableDataInfo<ConfigOptionVo> queryPageList(ConfigOptionBo bo, PageQuery pageQuery) {
        return page(configOptionMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "option_id"));
    }

    @Override
    public List<ConfigOptionVo> queryList(ConfigOptionBo bo) {
        return configOptionMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "option_id")));
    }

    @Override
    public ConfigOptionVo queryById(Long id) {
        return configOptionMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ConfigOptionBo bo) {
        ConfigOption entity = MapstructUtils.convert(bo, ConfigOption.class);
        if (entity == null) { return Boolean.FALSE; }
        ProductEntityDefaults.prepareInsert(entity);
        return configOptionMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ConfigOptionBo bo) {
        ConfigOption entity = MapstructUtils.convert(bo, ConfigOption.class);
        if (entity == null) { return Boolean.FALSE; }
        return configOptionMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(configOptionMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return configOptionMapper.update(null, new LambdaUpdateWrapper<ConfigOption>()
            .eq(ConfigOption::getOptionId, id)
            .set(ConfigOption::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        ConfigOption option = configOptionMapper.selectById(id);
        if (option == null || StringUtils.isBlank(option.getOptionCode())) { return referenceResult(0, null, null); }
        long count = ruleMapper.selectCount(activeQuery(ConfigRule.class).like("condition_json", option.getOptionCode()))
            + ruleMapper.selectCount(activeQuery(ConfigRule.class).like("action_json", option.getOptionCode()));
        return referenceResult(count, "product.configOption.hasReferences", "Config rules mentioning option: " + count);
    }

    private QueryWrapper<ConfigOption> buildQueryWrapper(ConfigOptionBo bo) {
        QueryWrapper<ConfigOption> q = activeQuery(ConfigOption.class);
        if (bo != null) {
        eq(q, "question_id", bo.getQuestionId());
        eq(q, "template_version_id", bo.getTemplateVersionId());
        like(q, "option_code", bo.getOptionCode());
        like(q, "option_name_cn", bo.getOptionNameCn());
        like(q, "option_name_en", bo.getOptionNameEn());
        like(q, "status", bo.getStatus());
        }
        return q;
    }

}
