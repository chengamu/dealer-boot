package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringRuleBo;
import com.bocoo.product.domain.entity.EngineeringRule;
import com.bocoo.product.domain.vo.EngineeringRuleVo;
import com.bocoo.product.mapper.EngineeringRuleMapper;
import com.bocoo.product.service.EngineeringRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineeringRuleServiceImpl extends ProductServiceSupport implements EngineeringRuleService {

    private final EngineeringRuleMapper ruleMapper;

    @Override
    public TableDataInfo<EngineeringRuleVo> queryPageList(EngineeringRuleBo query, PageQuery pageQuery) {
        QueryWrapper<EngineeringRule> q = activeQuery(EngineeringRule.class);
        if (query != null) {
            eq(q, "version_id", query.getVersionId());
            eq(q, "status", query.getStatus());
            like(q, "rule_code", query.getRuleCode());
            if (StringUtils.isNotBlank(query.getRuleNameCn())) {
                q.and(wrapper -> wrapper.like("rule_name_cn", query.getRuleNameCn()).or().like("rule_name_en", query.getRuleNameCn()));
            }
        }
        return page(ruleMapper, pageQuery, q.orderByAsc("sort_order").orderByDesc("update_time"));
    }

    @Override
    public EngineeringRuleVo queryById(Long id) {
        return ruleMapper.selectVoById(id);
    }

    @Override
    public Boolean save(EngineeringRuleBo bo) {
        return saveEntity(ruleMapper, bo, EngineeringRule::getRuleId);
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return remove(ruleMapper, ids);
    }
}
