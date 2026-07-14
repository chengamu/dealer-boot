package com.bocoo.common.oss.core;

import java.util.Locale;

/**
 * OSS 凭证来源。
 */
public enum OssCredentialMode {

    ACCESS_KEY("access_key", false),
    LOCAL_STS("local_sts", true),
    ECS_RAM_ROLE("ecs_ram_role", true);

    private final String value;
    private final boolean sharedBucket;

    OssCredentialMode(String value, boolean sharedBucket) {
        this.value = value;
        this.sharedBucket = sharedBucket;
    }

    public String getValue() {
        return value;
    }

    public boolean usesSharedBucket() {
        return sharedBucket;
    }

    public static OssCredentialMode from(String value) {
        if (value == null || value.isBlank()) {
            return ACCESS_KEY;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT);
        for (OssCredentialMode mode : values()) {
            if (mode.value.equals(normalized)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("Unsupported OSS credential mode: " + value);
    }
}
