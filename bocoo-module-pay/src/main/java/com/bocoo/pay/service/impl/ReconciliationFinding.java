package com.bocoo.pay.service.impl;

import com.bocoo.pay.enums.ReconciliationAnomalyType;
import com.bocoo.pay.enums.ReconciliationSeverity;

record ReconciliationFinding(ReconciliationAnomalyType type, ReconciliationSeverity severity,
                             String code, String message, String expectedJson, String actualJson) {
}
