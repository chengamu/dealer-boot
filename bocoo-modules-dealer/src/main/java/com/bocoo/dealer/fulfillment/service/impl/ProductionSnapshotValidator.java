package com.bocoo.dealer.fulfillment.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductionSnapshotValidator {
    public void validate(List<SalesDocumentItem> items) {
        boolean invalid = items.isEmpty() || items.stream().anyMatch(item -> item.getFormulaVersionId() == null
            || StringUtils.isBlank(item.getSelectedOptionsJson()) || StringUtils.isBlank(item.getBomSnapshotJson()));
        if (invalid) throw ServiceException.ofMessageKey("dealer.fulfillment.productionSnapshotIncomplete");
    }
}
