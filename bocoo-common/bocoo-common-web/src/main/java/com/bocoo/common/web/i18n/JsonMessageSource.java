package com.bocoo.common.web.i18n;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.AbstractMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Loads static JSON locale files from classpath:/i18n.
 */
@Slf4j
public class JsonMessageSource extends AbstractMessageSource {

    private static final String RESOURCE_PATTERN = "classpath*:i18n/*.json";
    private static final String DEFAULT_LOCALE = "en_US";
    private static final String ZH_CN_LOCALE = "zh_CN";

    private final Map<String, Map<String, String>> messages = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonMessageSource() {
        loadMessages();
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = resolveMessage(code, locale);
        return message == null ? null : createMessageFormat(message, locale);
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return resolveMessage(code, locale);
    }

    public String getRawMessage(String code, Locale locale) {
        return resolveMessage(code, locale);
    }

    private void loadMessages() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        try {
            Resource[] resources = resolver.getResources(RESOURCE_PATTERN);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null || !filename.endsWith(".json")) {
                    continue;
                }
                String locale = filename.substring(0, filename.length() - ".json".length());
                Map<String, String> localeMessages = objectMapper.readValue(
                    resource.getInputStream(),
                    new TypeReference<Map<String, String>>() {
                    });
                messages.computeIfAbsent(locale, key -> new ConcurrentHashMap<>()).putAll(localeMessages);
            }
            log.info("Loaded JSON i18n locales: {}", messages.keySet());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load JSON i18n resources", e);
        }
    }

    private String resolveMessage(String code, Locale locale) {
        for (String localeKey : candidateLocales(locale)) {
            Map<String, String> localeMessages = messages.get(localeKey);
            if (localeMessages != null && localeMessages.containsKey(code)) {
                return localeMessages.get(code);
            }
        }
        return null;
    }

    private List<String> candidateLocales(Locale locale) {
        Set<String> candidates = new LinkedHashSet<>();
        if (locale != null) {
            String normalized = locale.toString().replace('-', '_');
            if (!normalized.isBlank()) {
                candidates.add(normalized);
            }
            String language = locale.getLanguage();
            if ("zh".equalsIgnoreCase(language)) {
                candidates.add(ZH_CN_LOCALE);
            } else if ("en".equalsIgnoreCase(language)) {
                candidates.add(DEFAULT_LOCALE);
            }
        }
        candidates.add(DEFAULT_LOCALE);
        candidates.add(ZH_CN_LOCALE);
        return new ArrayList<>(candidates);
    }
}
