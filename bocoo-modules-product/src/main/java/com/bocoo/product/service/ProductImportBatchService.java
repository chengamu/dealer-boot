package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductImportBatchBo;
import com.bocoo.product.domain.vo.ProductImportBatchVo;

public interface ProductImportBatchService {

    TableDataInfo<ProductImportBatchVo> queryPageList(ProductImportBatchBo bo, PageQuery pageQuery);

    ProductImportBatchVo queryById(Long batchId);

    Boolean save(ProductImportBatchBo bo);

    Boolean updateStatus(Long batchId, String status);

    Boolean deleteByIds(Long[] ids);
}
