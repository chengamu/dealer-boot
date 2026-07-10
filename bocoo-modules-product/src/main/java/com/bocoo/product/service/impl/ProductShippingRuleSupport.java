package com.bocoo.product.service.impl;

final class ProductShippingRuleSupport {

    static final String CODE_MANUAL = "MANUAL";
    static final String CODE_MOTORIZED = "MOTORIZED";

    private ProductShippingRuleSupport() {
    }

    static String feeName(String code) {
        return CODE_MOTORIZED.equals(code) ? "带电邮费" : "不带电邮费";
    }
}
