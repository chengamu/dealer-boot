package com.bocoo.common.core.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class TimeUtils {

    private TimeUtils() {
    }

    public static LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }
}
