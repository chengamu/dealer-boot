package com.bocoo.common.core.utils;

import java.time.format.DateTimeFormatter;

public class GenerateNoUtil {

    private static String generateTimePrefix() {
        return TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("MMdd"));
    }
}
