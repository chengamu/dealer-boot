package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductMaterialService {

    TableDataInfo<ProductMaterialVo> queryPageList(ProductMaterialBo bo, PageQuery pageQuery);

    List<ProductMaterialVo> queryList(ProductMaterialBo bo);

    ProductMaterialVo queryById(Long id);

    Boolean insertByBo(ProductMaterialBo bo);

    Boolean updateByBo(ProductMaterialBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long materialId);
}
