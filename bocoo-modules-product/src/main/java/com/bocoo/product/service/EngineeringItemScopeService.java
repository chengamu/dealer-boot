package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringItemScopeBo;
import com.bocoo.product.domain.vo.EngineeringItemScopeVo;

public interface EngineeringItemScopeService {

    TableDataInfo<EngineeringItemScopeVo> queryPageList(EngineeringItemScopeBo query, PageQuery pageQuery);

    EngineeringItemScopeVo queryById(Long id);

    Boolean save(EngineeringItemScopeBo bo);

    Boolean deleteByIds(Long[] ids);
}
