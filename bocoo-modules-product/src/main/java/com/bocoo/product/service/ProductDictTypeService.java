package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductDictTypeBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductDictTypeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductDictTypeService {

    TableDataInfo<ProductDictTypeVo> queryPageList(ProductDictTypeBo bo, PageQuery pageQuery);

    List<ProductDictTypeVo> queryList(ProductDictTypeBo bo);

    ProductDictTypeVo queryById(Long id);

    Boolean insertByBo(ProductDictTypeBo bo);

    Boolean updateByBo(ProductDictTypeBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    BaseEditCheckResultVo checkEditAllowed(Long id);

    ReferenceCheckResultVo checkReferences(Long id);
}
