package com.bocoo.common.web.i18n;

import com.bocoo.common.core.service.I18nService;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Map;

/**
 * Runtime i18n service backed by the configured MessageSource.
 */
public class JsonI18nService implements I18nService {

    private final MessageSource messageSource;

    public JsonI18nService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String get(String code, Object... args) {
        return get(LocaleContextHolder.getLocale(), code, args);
    }

    @Override
    public String get(Locale locale, String code, Object... args) {
        return messageSource.getMessage(code, args, locale);
    }

    @Override
    public String get(String code, Map<String, ?> args) {
        return get(LocaleContextHolder.getLocale(), code, args);
    }

    @Override
    public String get(Locale locale, String code, Map<String, ?> args) {
        String message = rawMessage(locale, code);
        if (message == null || args == null || args.isEmpty()) {
            return message;
        }
        String result = message;
        for (Map.Entry<String, ?> entry : args.entrySet()) {
            String value = String.valueOf(entry.getValue());
            result = result
                .replace("{{" + entry.getKey() + "}}", value)
                .replace("{" + entry.getKey() + "}", value);
        }
        return result;
    }

    private String rawMessage(Locale locale, String code) {
        if (messageSource instanceof JsonMessageSource jsonMessageSource) {
            return jsonMessageSource.getRawMessage(code, locale);
        }
        return messageSource.getMessage(code, null, locale);
    }
}
