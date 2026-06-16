package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigRuleBo;
import com.bocoo.product.domain.vo.ConfigRuleVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ConfigRuleService {

    TableDataInfo<ConfigRuleVo> queryPageList(ConfigRuleBo bo, PageQuery pageQuery);

    List<ConfigRuleVo> queryList(ConfigRuleBo bo);

    ConfigRuleVo queryById(Long id);

    Boolean insertByBo(ConfigRuleBo bo);

    Boolean updateByBo(ConfigRuleBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
