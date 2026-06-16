package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductBaseAttributeBo;
import com.bocoo.product.domain.vo.ProductBaseAttributeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductBaseAttributeService {

    TableDataInfo<ProductBaseAttributeVo> queryPageList(ProductBaseAttributeBo bo, PageQuery pageQuery);

    List<ProductBaseAttributeVo> queryList(ProductBaseAttributeBo bo);

    ProductBaseAttributeVo queryById(Long id);

    Boolean insertByBo(ProductBaseAttributeBo bo);

    Boolean updateByBo(ProductBaseAttributeBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long attributeId);
}
