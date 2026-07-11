package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.entity.SalesDocumentEvent;
import com.bocoo.dealer.mapper.SalesDocumentEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SalesDocumentEventRecorder {
    private final SalesDocumentEventMapper mapper;

    void record(Long documentId, Long tenantId, String eventType, String from, String to, String note) {
        SalesDocumentEvent event = new SalesDocumentEvent();
        event.setSalesDocumentId(documentId);
        event.setTenantId(tenantId);
        event.setEventType(eventType);
        event.setFromStatus(from);
        event.setToStatus(to);
        event.setOperatorId(LoginHelper.getUserId());
        event.setOperatorName(LoginHelper.getUsername());
        event.setEventNote(note);
        event.setOccurredTime(TimeUtils.utcNow());
        mapper.insert(event);
    }
}
