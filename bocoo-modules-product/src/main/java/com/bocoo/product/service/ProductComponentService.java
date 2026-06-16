package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductComponentBo;
import com.bocoo.product.domain.vo.ProductComponentVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ProductComponentService {

    TableDataInfo<ProductComponentVo> queryPageList(ProductComponentBo bo, PageQuery pageQuery);

    List<ProductComponentVo> queryList(ProductComponentBo bo);

    ProductComponentVo queryById(Long id);

    Boolean insertByBo(ProductComponentBo bo);

    Boolean updateByBo(ProductComponentBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long componentId);
}
