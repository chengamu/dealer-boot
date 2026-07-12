package com.bocoo.dealer.fulfillment.service;

public interface ReceiptService {
    Boolean confirm(Long shipmentId);

    Boolean override(Long shipmentId, String reason);
}
