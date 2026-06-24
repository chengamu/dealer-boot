package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialTypeBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialTypeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductMaterialTypeService {

    TableDataInfo<ProductMaterialTypeVo> queryPageList(ProductMaterialTypeBo bo, PageQuery pageQuery);

    List<ProductMaterialTypeVo> queryList(ProductMaterialTypeBo bo);

    ProductMaterialTypeVo queryById(Long id);

    ProductMaterialTypeVo queryByCode(String code);

    Boolean insertByBo(ProductMaterialTypeBo bo);

    Boolean updateByBo(ProductMaterialTypeBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    BaseEditCheckResultVo checkEditAllowed(Long id);

    ReferenceCheckResultVo checkReferences(Long materialTypeId);
}
