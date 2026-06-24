package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialTypeGroupBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialTypeGroupVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductMaterialTypeGroupService {

    TableDataInfo<ProductMaterialTypeGroupVo> queryPageList(ProductMaterialTypeGroupBo bo, PageQuery pageQuery);

    List<ProductMaterialTypeGroupVo> queryList(ProductMaterialTypeGroupBo bo);

    ProductMaterialTypeGroupVo queryById(Long id);

    Boolean insertByBo(ProductMaterialTypeGroupBo bo);

    Boolean updateByBo(ProductMaterialTypeGroupBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    BaseEditCheckResultVo checkEditAllowed(Long id);

    ReferenceCheckResultVo checkReferences(Long groupId);
}
