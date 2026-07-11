package com.bocoo.merchant.service.impl;

import java.util.Map;

record CustomerQuoteOptionSnapshot(
    Map<String, String> selectedValues,
    String summaryCn,
    String summaryEn,
    boolean englishComplete,
    boolean motorized
) {
}
