package com.bocoo.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TenantType {

    PLATFORM("PLATFORM"),
    MERCHANT("MERCHANT");

    private final String code;
}
