package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringCheckCaseBo;
import com.bocoo.product.domain.vo.EngineeringCheckCaseVo;

public interface EngineeringCheckCaseService {

    TableDataInfo<EngineeringCheckCaseVo> queryPageList(EngineeringCheckCaseBo query, PageQuery pageQuery);

    EngineeringCheckCaseVo queryById(Long id);

    Boolean save(EngineeringCheckCaseBo bo);

    Boolean deleteByIds(Long[] ids);
}
