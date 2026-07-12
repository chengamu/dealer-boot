package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocumentEvent;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FulfillmentEventRecorder {
    private final SalesDocumentEventMapper mapper;
    private final FulfillmentAccessSupport access;

    public void record(Long documentId, Long tenantId, String type,
                       String from, String to, String note) {
        SalesDocumentEvent row = new SalesDocumentEvent();
        row.setSalesDocumentId(documentId);
        row.setTenantId(tenantId);
        row.setEventType(type);
        row.setFromStatus(from);
        row.setToStatus(to);
        row.setOperatorId(LoginHelper.getUserId());
        row.setOperatorName(LoginHelper.getUsername());
        row.setEventNote(note);
        row.setOccurredTime(TimeUtils.utcNow());
        access.ignoreTenant(() -> mapper.insert(row));
    }
}
