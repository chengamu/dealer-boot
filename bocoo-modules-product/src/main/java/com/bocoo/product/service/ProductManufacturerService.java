package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductManufacturerBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductManufacturerVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductManufacturerService {

    TableDataInfo<ProductManufacturerVo> queryPageList(ProductManufacturerBo bo, PageQuery pageQuery);

    List<ProductManufacturerVo> queryList(ProductManufacturerBo bo);

    ProductManufacturerVo queryById(Long id);

    Boolean insertByBo(ProductManufacturerBo bo);

    Boolean updateByBo(ProductManufacturerBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    BaseEditCheckResultVo checkEditAllowed(Long id);

    ReferenceCheckResultVo checkReferences(Long manufacturerId);
}
