package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductImportRowIssueBo;
import com.bocoo.product.domain.vo.ProductImportRowIssueVo;

public interface ProductImportRowIssueService {

    TableDataInfo<ProductImportRowIssueVo> queryPageList(ProductImportRowIssueBo bo, PageQuery pageQuery);

    ProductImportRowIssueVo queryById(Long issueId);

    Boolean save(ProductImportRowIssueBo bo);

    Boolean deleteByIds(Long[] ids);
}
