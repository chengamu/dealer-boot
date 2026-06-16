package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringOutputRuleBo;
import com.bocoo.product.domain.entity.EngineeringOutputRule;
import com.bocoo.product.domain.vo.EngineeringOutputRuleVo;
import com.bocoo.product.mapper.EngineeringOutputRuleMapper;
import com.bocoo.product.service.EngineeringOutputRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineeringOutputRuleServiceImpl extends ProductServiceSupport implements EngineeringOutputRuleService {

    private final EngineeringOutputRuleMapper outputRuleMapper;

    @Override
    public TableDataInfo<EngineeringOutputRuleVo> queryPageList(EngineeringOutputRuleBo query, PageQuery pageQuery) {
        QueryWrapper<EngineeringOutputRule> q = activeQuery(EngineeringOutputRule.class);
        if (query != null) {
            eq(q, "version_id", query.getVersionId());
            eq(q, "status", query.getStatus());
            like(q, "rule_code", query.getRuleCode());
            like(q, "output_code", query.getOutputCode());
        }
        return page(outputRuleMapper, pageQuery, q.orderByAsc("sort_order").orderByDesc("update_time"));
    }

    @Override
    public EngineeringOutputRuleVo queryById(Long id) {
        return outputRuleMapper.selectVoById(id);
    }

    @Override
    public Boolean save(EngineeringOutputRuleBo bo) {
        return saveEntity(outputRuleMapper, bo, EngineeringOutputRule::getOutputRuleId);
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return remove(outputRuleMapper, ids);
    }
}
