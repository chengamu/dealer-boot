package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PricePlanBo;
import com.bocoo.product.domain.vo.PricePlanVo;

import java.util.List;

public interface PricePlanService {

    TableDataInfo<PricePlanVo> queryPageList(PricePlanBo bo, PageQuery pageQuery);

    List<PricePlanVo> queryList(PricePlanBo bo);

    PricePlanVo queryById(Long id);

    Boolean insertByBo(PricePlanBo bo);

    Boolean updateByBo(PricePlanBo bo);

    Boolean deleteWithValidByIds(Long[] ids);
}
