package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.SalesProductBo;
import com.bocoo.product.domain.vo.SalesProductVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface SalesProductService {

    TableDataInfo<SalesProductVo> queryPageList(SalesProductBo bo, PageQuery pageQuery);

    List<SalesProductVo> queryList(SalesProductBo bo);

    SalesProductVo queryById(Long id);

    Boolean insertByBo(SalesProductBo bo);

    Boolean updateByBo(SalesProductBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
