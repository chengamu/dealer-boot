package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.vo.ProductCategoryVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductCategoryService {

    TableDataInfo<ProductCategoryVo> queryPageList(ProductCategoryBo bo, PageQuery pageQuery);

    List<ProductCategoryVo> queryList(ProductCategoryBo bo);

    ProductCategoryVo queryById(Long id);

    Boolean insertByBo(ProductCategoryBo bo);

    Boolean updateByBo(ProductCategoryBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long categoryId);
}
