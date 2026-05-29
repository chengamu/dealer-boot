package com.bocoo.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.system.domain.entity.SysCountry;
import com.bocoo.system.domain.entity.SysCurrency;
import com.bocoo.system.domain.entity.SysLanguage;
import com.bocoo.system.domain.vo.SysCountryVo;
import com.bocoo.system.domain.vo.SysCurrencyVo;
import com.bocoo.system.domain.vo.SysLanguageVo;
import com.bocoo.system.mapper.SysCountryMapper;
import com.bocoo.system.mapper.SysCurrencyMapper;
import com.bocoo.system.mapper.SysLanguageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

@RequiredArgsConstructor
@Service
public class SysStandardDataService {

    private final SysCountryMapper countryMapper;
    private final SysCurrencyMapper currencyMapper;
    private final SysLanguageMapper languageMapper;

    public List<SysCountryVo> listCountries(String keyword) {
        List<SysCountryVo> list = countryMapper.selectVoList(new LambdaQueryWrapper<SysCountry>()
            .eq(SysCountry::getStatus, UserConstants.DICT_NORMAL)
            .orderByAsc(SysCountry::getSort)
            .orderByAsc(SysCountry::getCountryCode));
        list.forEach(country -> country.setName(resolveName(country.getNameEn(), country.getNameZh())));
        return filter(list, keyword, country -> matches(keyword, country.getCountryCode(), country.getName(), country.getNameEn(), country.getNameZh()));
    }

    public List<SysCurrencyVo> listCurrencies(String keyword) {
        List<SysCurrencyVo> list = currencyMapper.selectVoList(new LambdaQueryWrapper<SysCurrency>()
            .eq(SysCurrency::getStatus, UserConstants.DICT_NORMAL)
            .orderByAsc(SysCurrency::getSort)
            .orderByAsc(SysCurrency::getCurrencyCode));
        list.forEach(currency -> currency.setName(resolveName(currency.getNameEn(), currency.getNameZh())));
        return filter(list, keyword, currency -> matches(keyword, currency.getCurrencyCode(), currency.getName(), currency.getNameEn(), currency.getNameZh()));
    }

    public List<SysLanguageVo> listLanguages(String keyword) {
        List<SysLanguageVo> list = languageMapper.selectVoList(new LambdaQueryWrapper<SysLanguage>()
            .eq(SysLanguage::getStatus, UserConstants.DICT_NORMAL)
            .orderByAsc(SysLanguage::getSort)
            .orderByAsc(SysLanguage::getLanguageCode));
        list.forEach(language -> language.setName(resolveName(language.getNameEn(), language.getNameNative())));
        return filter(list, keyword, language -> matches(keyword, language.getLanguageCode(), language.getName(), language.getNameEn(), language.getNameNative()));
    }

    private <T> List<T> filter(List<T> list, String keyword, Predicate<T> predicate) {
        if (StringUtils.isBlank(keyword)) {
            return list;
        }
        return list.stream().filter(predicate).toList();
    }

    private boolean matches(String keyword, String... values) {
        String normalizedKeyword = normalize(keyword);
        for (String value : values) {
            if (normalize(value).contains(normalizedKeyword)) {
                return true;
            }
        }
        return false;
    }

    private String resolveName(String englishName, String localizedName) {
        Locale locale = LocaleContextHolder.getLocale();
        if (locale != null && "zh".equalsIgnoreCase(locale.getLanguage())) {
            return StringUtils.blankToDefault(localizedName, englishName);
        }
        return StringUtils.blankToDefault(englishName, localizedName);
    }

    private String normalize(String value) {
        return StringUtils.blankToDefault(value, "").toLowerCase(Locale.ROOT);
    }
}
