package com.bocoo.dealer.quickorder.service;

import com.bocoo.dealer.quickorder.domain.bo.QuickOrderSubmitBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderSubmitResultVo;

public interface QuickOrderSubmissionService {
    QuickOrderSubmitResultVo submit(Long quickOrderId, QuickOrderSubmitBo bo);
}
