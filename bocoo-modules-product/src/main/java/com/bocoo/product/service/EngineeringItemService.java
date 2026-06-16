package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.EngineeringItemBo;
import com.bocoo.product.domain.vo.EngineeringItemVo;

public interface EngineeringItemService {

    TableDataInfo<EngineeringItemVo> queryPageList(EngineeringItemBo query, PageQuery pageQuery);

    EngineeringItemVo queryById(Long id);

    Boolean save(EngineeringItemBo bo);

    Boolean deleteByIds(Long[] ids);
}
