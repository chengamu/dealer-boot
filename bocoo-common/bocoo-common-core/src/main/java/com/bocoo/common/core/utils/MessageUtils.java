package com.bocoo.common.core.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.bocoo.common.core.service.I18nService;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;
import java.util.Map;

/**
 * 获取i18n资源文件
 *
 * @author Lion Li
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageUtils {

    private static final I18nService I18N_SERVICE = SpringUtils.getBean(I18nService.class);

    /**
     * 根据消息键和参数 获取消息 委托给spring messageSource
     *
     * @param code 消息键
     * @param args 参数
     * @return 获取国际化翻译值
     */
    public static String message(String code, Object... args) {
        return I18N_SERVICE.get(LocaleContextHolder.getLocale(), code, args);
    }

    public static String message(Locale locale, String code, Object... args) {
        return I18N_SERVICE.get(locale, code, args);
    }

    public static String message(String code, Map<String, ?> args) {
        return I18N_SERVICE.get(LocaleContextHolder.getLocale(), code, args);
    }

    public static String message(Locale locale, String code, Map<String, ?> args) {
        return I18N_SERVICE.get(locale, code, args);
    }
}
