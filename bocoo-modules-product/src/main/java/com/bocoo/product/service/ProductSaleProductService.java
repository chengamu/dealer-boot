package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;

import java.util.List;

public interface ProductSaleProductService {
    TableDataInfo<ProductSaleProductVo> queryPageList(ProductSaleProductBo bo, PageQuery pageQuery);

    List<ProductSaleProductVo> queryList(ProductSaleProductBo bo);

    ProductSaleProductVo queryById(Long id);

    Boolean insertByBo(ProductSaleProductBo bo);

    Boolean updateByBo(ProductSaleProductBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
