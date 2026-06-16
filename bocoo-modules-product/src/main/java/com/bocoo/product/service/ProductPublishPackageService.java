package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductPublishPackageBo;
import com.bocoo.product.domain.vo.ProductPublishPackageVo;

import java.util.List;

public interface ProductPublishPackageService {

    TableDataInfo<ProductPublishPackageVo> queryPageList(ProductPublishPackageBo bo, PageQuery pageQuery);

    List<ProductPublishPackageVo> queryList(ProductPublishPackageBo bo);

    ProductPublishPackageVo queryById(Long id);

    Boolean save(ProductPublishPackageBo bo);

    Boolean deleteByIds(Long[] ids);
}
