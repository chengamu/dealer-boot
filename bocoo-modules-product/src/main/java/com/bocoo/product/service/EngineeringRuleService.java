package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringRuleBo;
import com.bocoo.product.domain.vo.EngineeringRuleVo;

public interface EngineeringRuleService {

    TableDataInfo<EngineeringRuleVo> queryPageList(EngineeringRuleBo query, PageQuery pageQuery);

    EngineeringRuleVo queryById(Long id);

    Boolean save(EngineeringRuleBo bo);

    Boolean deleteByIds(Long[] ids);
}
