package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PricePlanVersionBo;
import com.bocoo.product.domain.vo.PricePlanVersionVo;

import java.util.List;

public interface PricePlanVersionService {

    TableDataInfo<PricePlanVersionVo> queryPageList(PricePlanVersionBo bo, PageQuery pageQuery);

    List<PricePlanVersionVo> queryList(PricePlanVersionBo bo);

    PricePlanVersionVo queryById(Long id);

    Boolean insertByBo(PricePlanVersionBo bo);

    Boolean updateByBo(PricePlanVersionBo bo);

    Boolean deleteWithValidByIds(Long[] ids);
}
