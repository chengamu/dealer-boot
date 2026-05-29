package com.bocoo.common.core.service;

import java.util.Locale;
import java.util.Map;

/**
 * JSON-backed i18n lookup service.
 */
public interface I18nService {

    String get(String code, Object... args);

    String get(Locale locale, String code, Object... args);

    String get(String code, Map<String, ?> args);

    String get(Locale locale, String code, Map<String, ?> args);
}
