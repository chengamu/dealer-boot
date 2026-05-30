package com.bocoo.pay.service;

import com.bocoo.pay.domain.bo.PayTransferCreateBo;
import com.bocoo.pay.domain.entity.PayTransfer;

/**
 * Payment transfer service contract.
 */
public interface PayTransferService {

    PayTransfer createTransfer(PayTransferCreateBo bo);
}
