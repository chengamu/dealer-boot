package com.bocoo.common.web.config;

import com.bocoo.common.web.core.I18nLocaleResolver;
import com.bocoo.common.core.service.I18nService;
import com.bocoo.common.web.i18n.JsonI18nService;
import com.bocoo.common.web.i18n.JsonMessageSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.LocaleResolver;

/**
 * 国际化配置
 *
 * @author Lion Li
 */
@AutoConfiguration(before = WebMvcAutoConfiguration.class)
public class I18nConfig {

    @Bean
    public LocaleResolver localeResolver() {
        return new I18nLocaleResolver();
    }

    @Bean
    public MessageSource messageSource() {
        return new JsonMessageSource();
    }

    @Bean
    public I18nService i18nService(MessageSource messageSource) {
        return new JsonI18nService(messageSource);
    }

}
