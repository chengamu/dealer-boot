package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialAttributeBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductMaterialAttributeService {

    TableDataInfo<ProductMaterialAttributeVo> queryPageList(ProductMaterialAttributeBo bo, PageQuery pageQuery);

    List<ProductMaterialAttributeVo> queryList(ProductMaterialAttributeBo bo);

    ProductMaterialAttributeVo queryById(Long id);

    Boolean insertByBo(ProductMaterialAttributeBo bo);

    Boolean updateByBo(ProductMaterialAttributeBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    BaseEditCheckResultVo checkEditAllowed(Long id);

    ReferenceCheckResultVo checkReferences(Long id);
}
