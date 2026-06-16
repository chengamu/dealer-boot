package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringPlanBo;
import com.bocoo.product.domain.vo.EngineeringPlanVo;

import java.util.List;

public interface EngineeringPlanService {

    TableDataInfo<EngineeringPlanVo> queryPageList(EngineeringPlanBo query, PageQuery pageQuery);

    List<EngineeringPlanVo> queryList(EngineeringPlanBo query);

    EngineeringPlanVo queryById(Long id);

    Boolean save(EngineeringPlanBo bo);

    Boolean deleteByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);
}
