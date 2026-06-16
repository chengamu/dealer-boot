package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductComponentItemBo;
import com.bocoo.product.domain.vo.ProductComponentItemVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductComponentItemService {

    TableDataInfo<ProductComponentItemVo> queryPageList(ProductComponentItemBo bo, PageQuery pageQuery);

    List<ProductComponentItemVo> queryList(ProductComponentItemBo bo);

    ProductComponentItemVo queryById(Long id);

    Boolean insertByBo(ProductComponentItemBo bo);

    Boolean updateByBo(ProductComponentItemBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
