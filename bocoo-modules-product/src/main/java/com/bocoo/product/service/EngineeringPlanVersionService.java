package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringPlanVersionBo;
import com.bocoo.product.domain.vo.EngineeringPlanVersionVo;

public interface EngineeringPlanVersionService {

    TableDataInfo<EngineeringPlanVersionVo> queryPageList(EngineeringPlanVersionBo query, PageQuery pageQuery);

    EngineeringPlanVersionVo queryById(Long id);

    Boolean save(EngineeringPlanVersionBo bo);

    Boolean deleteByIds(Long[] ids);
}
