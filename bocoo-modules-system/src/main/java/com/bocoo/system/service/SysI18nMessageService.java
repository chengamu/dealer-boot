package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.system.domain.entity.SysI18nMessage;
import com.bocoo.system.mapper.SysI18nMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 国际化消息 业务层处理
 */
@RequiredArgsConstructor
@Service
public class SysI18nMessageService {

    private final SysI18nMessageMapper i18nMessageMapper;
    private final Map<String, String> messageCache = new ConcurrentHashMap<>();

    public String translate(String i18nKey, String fallback) {
        if (StringUtils.isBlank(i18nKey)) {
            return fallback;
        }
        String locale = normalizeLocale(LocaleContextHolder.getLocale());
        String cacheKey = locale + ":" + i18nKey;
        String message = messageCache.computeIfAbsent(cacheKey, key -> selectMessage(i18nKey, locale));
        return StringUtils.isNotBlank(message) ? message : fallback;
    }

    private String selectMessage(String i18nKey, String locale) {
        SysI18nMessage message = i18nMessageMapper.selectOne(new LambdaQueryWrapper<SysI18nMessage>()
            .eq(SysI18nMessage::getMessageKey, i18nKey)
            .eq(SysI18nMessage::getLocale, locale), false);
        if (message == null && !"en_US".equals(locale)) {
            message = i18nMessageMapper.selectOne(new LambdaQueryWrapper<SysI18nMessage>()
                .eq(SysI18nMessage::getMessageKey, i18nKey)
                .eq(SysI18nMessage::getLocale, "en_US"), false);
        }
        return message == null ? null : message.getMessageValue();
    }

    private String normalizeLocale(Locale locale) {
        if (locale == null) {
            return "en_US";
        }
        String language = locale.getLanguage();
        String country = locale.getCountry();
        if ("zh".equalsIgnoreCase(language)) {
            return "zh_CN";
        }
        if ("en".equalsIgnoreCase(language)) {
            return "en_US";
        }
        return StringUtils.isBlank(country) ? language : language + "_" + country;
    }
}
