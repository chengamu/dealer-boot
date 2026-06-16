package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringOutputRuleBo;
import com.bocoo.product.domain.vo.EngineeringOutputRuleVo;

public interface EngineeringOutputRuleService {

    TableDataInfo<EngineeringOutputRuleVo> queryPageList(EngineeringOutputRuleBo query, PageQuery pageQuery);

    EngineeringOutputRuleVo queryById(Long id);

    Boolean save(EngineeringOutputRuleBo bo);

    Boolean deleteByIds(Long[] ids);
}
