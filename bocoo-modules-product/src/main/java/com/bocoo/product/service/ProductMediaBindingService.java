package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMediaBindingBo;
import com.bocoo.product.domain.vo.ProductMediaBindingVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductMediaBindingService {

    TableDataInfo<ProductMediaBindingVo> queryPageList(ProductMediaBindingBo bo, PageQuery pageQuery);

    List<ProductMediaBindingVo> queryList(ProductMediaBindingBo bo);

    ProductMediaBindingVo queryById(Long id);

    Boolean insertByBo(ProductMediaBindingBo bo);

    Boolean updateByBo(ProductMediaBindingBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long bindingId);
}
