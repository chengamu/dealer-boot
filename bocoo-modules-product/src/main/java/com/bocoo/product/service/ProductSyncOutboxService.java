package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductSyncOutboxBo;
import com.bocoo.product.domain.vo.ProductSyncOutboxVo;

import java.util.List;

public interface ProductSyncOutboxService {

    TableDataInfo<ProductSyncOutboxVo> queryPageList(ProductSyncOutboxBo bo, PageQuery pageQuery);

    List<ProductSyncOutboxVo> queryList(ProductSyncOutboxBo bo);

    ProductSyncOutboxVo queryById(Long id);

    Boolean save(ProductSyncOutboxBo bo);

    Boolean deleteByIds(Long[] ids);

    Boolean retry(Long outboxId);
}
