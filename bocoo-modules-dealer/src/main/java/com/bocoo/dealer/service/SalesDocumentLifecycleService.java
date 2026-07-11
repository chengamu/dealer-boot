package com.bocoo.dealer.service;

import com.bocoo.dealer.domain.bo.SalesPaymentBo;
import com.bocoo.dealer.domain.bo.SalesShipmentBo;

public interface SalesDocumentLifecycleService {
    Boolean cancel(Long id, String reason);
    Boolean confirmPayment(Long id, SalesPaymentBo bo);
    Boolean startProduction(Long id);
    Boolean completeProduction(Long id);
    Boolean ship(Long id, SalesShipmentBo bo);
    Boolean deliver(Long id);
}
