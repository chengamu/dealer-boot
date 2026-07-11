package com.bocoo.dealer.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.vo.SalesDocumentEventVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;

import java.util.List;

public interface SalesDocumentQueryService {
    TableDataInfo<SalesDocumentVo> queryPage(SalesDocumentBo bo, PageQuery pageQuery);
    SalesDocumentVo queryById(Long id);
    List<SalesDocumentEventVo> events(Long id);
    List<SalesDocumentVo> customerHistory(Long customerId);
}
