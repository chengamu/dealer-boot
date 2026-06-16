package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.entity.ProductSnapshotInstance;
import com.bocoo.product.domain.vo.ProductSnapshotInstanceVo;

public interface ProductSnapshotInstanceService {

    TableDataInfo<ProductSnapshotInstanceVo> queryPageList(ProductSnapshotInstance query, PageQuery pageQuery);

    ProductSnapshotInstanceVo queryById(Long snapshotId);
}
