package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMediaAssetBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMediaAssetVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductMediaAssetService {

    TableDataInfo<ProductMediaAssetVo> queryPageList(ProductMediaAssetBo bo, PageQuery pageQuery);

    List<ProductMediaAssetVo> queryList(ProductMediaAssetBo bo);

    ProductMediaAssetVo queryById(Long id);

    Boolean insertByBo(ProductMediaAssetBo bo);

    Boolean updateByBo(ProductMediaAssetBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    BaseEditCheckResultVo checkEditAllowed(Long id);

    ReferenceCheckResultVo checkReferences(Long assetId);
}
