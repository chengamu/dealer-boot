package com.bocoo.dealer.service;

import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.bo.SalesDocumentItemBo;
import com.bocoo.dealer.domain.vo.SalesDocumentItemVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;

public interface SalesDocumentDraftService {
    Long insert(SalesDocumentBo bo);
    Boolean update(SalesDocumentBo bo);
    Long copy(Long id);
    Boolean delete(Long[] ids);
    SalesDocumentItemVo calculateItem(SalesDocumentItemBo bo);
    SalesDocumentVo calculateAll(Long id);
}
