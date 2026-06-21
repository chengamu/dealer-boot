package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringItemScopeBo;
import com.bocoo.product.domain.entity.EngineeringItemScope;
import com.bocoo.product.domain.vo.EngineeringItemScopeVo;
import com.bocoo.product.mapper.EngineeringItemScopeMapper;
import com.bocoo.product.service.EngineeringItemScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EngineeringItemScopeServiceImpl extends ProductServiceSupport implements EngineeringItemScopeService {

    private final EngineeringItemScopeMapper scopeMapper;

    @Override
    public TableDataInfo<EngineeringItemScopeVo> queryPageList(EngineeringItemScopeBo query, PageQuery pageQuery) {
        QueryWrapper<EngineeringItemScope> q = activeQuery(EngineeringItemScope.class);
        if (query != null) {
            eq(q, "version_id", query.getVersionId());
            eq(q, "status", query.getStatus());
            like(q, "item_code", query.getItemCode());
            like(q, "scope_code", query.getScopeCode());
            if (StringUtils.isNotBlank(query.getScopeNameCn())) {
                q.and(wrapper -> wrapper.like("scope_name_cn", query.getScopeNameCn()).or().like("scope_name_en", query.getScopeNameCn()));
            }
        }
        return page(scopeMapper, pageQuery, q, wrapper -> wrapper.orderByAsc("sort_order").orderByDesc("update_time"));
    }

    @Override
    public EngineeringItemScopeVo queryById(Long id) {
        return scopeMapper.selectVoById(id);
    }

    @Override
    public Boolean save(EngineeringItemScopeBo bo) {
        return saveEntity(scopeMapper, bo, EngineeringItemScope::getScopeId);
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return remove(scopeMapper, ids);
    }
}
