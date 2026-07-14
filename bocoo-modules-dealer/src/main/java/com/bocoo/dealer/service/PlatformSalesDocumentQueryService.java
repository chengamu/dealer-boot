package com.bocoo.dealer.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.domain.vo.SalesDocumentExportVo;

import java.util.List;

public interface PlatformSalesDocumentQueryService {
    TableDataInfo<SalesDocumentVo> queryPage(SalesDocumentBo bo, PageQuery pageQuery);

    SalesDocumentVo queryById(Long id);

    byte[] pdf(Long id);

    List<SalesDocumentExportVo> export(Long id);
}
