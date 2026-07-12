package com.bocoo.dealer.quickorder.service;

import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;

public interface QuickOrderDraftService {
    Long insert(QuickOrderBo bo);

    Boolean update(QuickOrderBo bo);

    Boolean delete(Long[] ids);

    Long copy(Long id);
}
