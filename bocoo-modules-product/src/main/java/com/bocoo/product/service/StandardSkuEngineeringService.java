package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.StandardSkuEngineeringBo;
import com.bocoo.product.domain.vo.StandardSkuEngineeringVo;

public interface StandardSkuEngineeringService {

    TableDataInfo<StandardSkuEngineeringVo> queryPageList(StandardSkuEngineeringBo query, PageQuery pageQuery);

    StandardSkuEngineeringVo queryById(Long id);

    Boolean save(StandardSkuEngineeringBo bo);

    Boolean deleteByIds(Long[] ids);
}
