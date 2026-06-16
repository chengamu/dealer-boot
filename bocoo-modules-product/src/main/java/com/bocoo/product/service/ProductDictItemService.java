package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductDictItemBo;
import com.bocoo.product.domain.vo.ProductDictItemVo;
import com.bocoo.product.domain.vo.ProductDictOptionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductDictItemService {

    TableDataInfo<ProductDictItemVo> queryPageList(ProductDictItemBo bo, PageQuery pageQuery);

    List<ProductDictItemVo> queryList(ProductDictItemBo bo);

    List<ProductDictOptionVo> queryOptionsByType(String dictTypeCode);

    ProductDictItemVo queryById(Long id);

    Boolean insertByBo(ProductDictItemBo bo);

    Boolean updateByBo(ProductDictItemBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
