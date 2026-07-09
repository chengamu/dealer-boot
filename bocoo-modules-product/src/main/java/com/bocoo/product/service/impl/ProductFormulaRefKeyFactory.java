package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;

import java.util.Locale;
import java.util.UUID;

final class ProductFormulaRefKeyFactory {
    private ProductFormulaRefKeyFactory() {
    }

    static String optionRefKey(String current, Long optionId) {
        return refKey(current, optionId, "OPT");
    }

    static String valueRefKey(String current, Long valueId) {
        return refKey(current, valueId, "VAL");
    }

    static String newOptionRefKey() {
        return newRefKey("OPT");
    }

    static String newValueRefKey() {
        return newRefKey("VAL");
    }

    private static String refKey(String current, Long id, String prefix) {
        if (StringUtils.isNotBlank(current)) {
            return current.trim();
        }
        if (id != null) {
            return prefix + "_" + id;
        }
        return newRefKey(prefix);
    }

    private static String newRefKey(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().replace("-", "").toUpperCase(Locale.ROOT);
    }
}
