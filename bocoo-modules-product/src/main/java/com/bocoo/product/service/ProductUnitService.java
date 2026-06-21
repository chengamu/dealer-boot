package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductUnitBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductUnitVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductUnitService {

    TableDataInfo<ProductUnitVo> queryPageList(ProductUnitBo bo, PageQuery pageQuery);

    List<ProductUnitVo> queryList(ProductUnitBo bo);

    ProductUnitVo queryById(Long id);

    Boolean insertByBo(ProductUnitBo bo);

    Boolean updateByBo(ProductUnitBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    BaseEditCheckResultVo checkEditAllowed(Long id);

    ReferenceCheckResultVo checkReferences(Long unitId);
}
