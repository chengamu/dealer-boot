package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductFormulaOptionBo;
import com.bocoo.product.domain.bo.ProductFormulaOptionValueBo;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ProductFormulaOptionSetupNormalizer extends ProductServiceSupport {
    private static final String VISIBILITY_ALWAYS = "ALWAYS";
    private static final String VISIBILITY_CONDITIONAL = "CONDITIONAL";
    private static final String DISPLAY_SELECT = "SELECT";
    private static final String DISPLAY_IMAGE_SELECT = "IMAGE_SELECT";
    private static final String HELP_LINK = "LINK";
    private static final String HELP_TEXT = "TEXT";
    private static final String HELP_RICH_TEXT = "RICH_TEXT";

    List<ProductFormulaOption> normalizeOptions(Long formulaId, List<ProductFormulaOptionBo> rows) {
        List<ProductFormulaOption> result = new ArrayList<>();
        Set<String> codes = new HashSet<>();
        int index = 0;
        for (ProductFormulaOptionBo row : rows == null ? List.<ProductFormulaOptionBo>of() : rows) {
            ProductFormulaOption entity = MapstructUtils.convert(row, ProductFormulaOption.class);
            if (entity == null) {
                continue;
            }
            entity.setFormulaId(formulaId);
            entity.setOptionRefKey(ProductFormulaRefKeyFactory.optionRefKey(entity.getOptionRefKey(), entity.getOptionId()));
            entity.setOptionCode(requiredUpper(entity.getOptionCode(), "product.formula.optionCodeRequired"));
            entity.setOptionNameCn(requiredTrim(entity.getOptionNameCn(), "product.formula.optionNameRequired"));
            entity.setOptionNameEn(trim(entity.getOptionNameEn()));
            if (!codes.add(entity.getOptionCode())) {
                throw ServiceException.ofMessageKey("product.formula.optionCodeDuplicate");
            }
            entity.setSourceType(defaultString(trim(entity.getSourceType()), "MANUAL"));
            entity.setSourceScope(trim(entity.getSourceScope()));
            entity.setSelectionMode(defaultString(trim(entity.getSelectionMode()), "SINGLE"));
            entity.setDisplayMode(normalizeDisplayMode(entity.getDisplayMode()));
            entity.setDefaultValueRefKey(trim(entity.getDefaultValueRefKey()));
            entity.setDefaultValueCode(trim(entity.getDefaultValueCode()));
            entity.setDefaultValueNameCn(trim(entity.getDefaultValueNameCn()));
            entity.setVisibilityMode(defaultString(trimUpper(row.getVisibilityMode()), VISIBILITY_ALWAYS));
            entity.setVisibleConditionOptionRefKey(trim(row.getVisibleConditionOptionRefKey()));
            entity.setVisibleConditionOptionCode(trimUpper(row.getVisibleConditionOptionCode()));
            entity.setVisibleConditionOptionNameCn(trim(row.getVisibleConditionOptionNameCn()));
            entity.setVisibleConditionValueRefKey(trim(row.getVisibleConditionValueRefKey()));
            entity.setVisibleConditionValueCode(trimUpper(row.getVisibleConditionValueCode()));
            entity.setVisibleConditionValueNameCn(trim(row.getVisibleConditionValueNameCn()));
            entity.setRequiredFlag(Boolean.TRUE.equals(entity.getRequiredFlag()));
            entity.setBusinessVisibleFlag(entity.getBusinessVisibleFlag() == null || Boolean.TRUE.equals(entity.getBusinessVisibleFlag()));
            entity.setHelpEnabled(Boolean.TRUE.equals(entity.getHelpEnabled()));
            entity.setHelpType(normalizeHelpType(entity.getHelpType()));
            entity.setHelpTitle(trim(entity.getHelpTitle()));
            entity.setHelpUrl(trim(entity.getHelpUrl()));
            if (HELP_TEXT.equals(entity.getHelpType())) {
                entity.setHelpUrl(null);
                entity.setHelpContent(stripHtml(entity.getHelpContent()));
            } else {
                entity.setHelpContent(null);
            }
            entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            result.add(entity);
            index++;
        }
        return result;
    }

    List<ProductFormulaOptionValue> normalizeOptionValues(Long formulaId, List<ProductFormulaOption> options,
                                                          List<ProductFormulaOptionValueBo> rows) {
        Map<String, ProductFormulaOption> optionByCode =
            options.stream().collect(Collectors.toMap(ProductFormulaOption::getOptionCode, Function.identity()));
        Map<String, ProductFormulaOption> optionByRef = options.stream()
            .collect(Collectors.toMap(ProductFormulaOption::getOptionRefKey, Function.identity(), (left, right) -> left));
        Set<String> keys = new HashSet<>();
        List<ProductFormulaOptionValue> result = new ArrayList<>();
        int index = 0;
        for (ProductFormulaOptionValueBo row : rows == null ? List.<ProductFormulaOptionValueBo>of() : rows) {
            ProductFormulaOptionValue entity = MapstructUtils.convert(row, ProductFormulaOptionValue.class);
            if (entity == null) {
                continue;
            }
            entity.setFormulaId(formulaId);
            entity.setOptionRefKey(trim(entity.getOptionRefKey()));
            entity.setOptionCode(trimUpper(entity.getOptionCode()));
            ProductFormulaOption option = StringUtils.isBlank(entity.getOptionRefKey()) ? null : optionByRef.get(entity.getOptionRefKey());
            if (option == null && StringUtils.isNotBlank(entity.getOptionCode())) {
                option = optionByCode.get(entity.getOptionCode());
            }
            if (option == null) {
                throw ServiceException.ofMessageKey("product.formula.optionValueOptionInvalid");
            }
            entity.setOptionId(option.getOptionId());
            entity.setOptionRefKey(option.getOptionRefKey());
            entity.setOptionCode(option.getOptionCode());
            entity.setValueRefKey(ProductFormulaRefKeyFactory.valueRefKey(entity.getValueRefKey(), entity.getOptionValueId()));
            entity.setValueCode(requiredUpper(entity.getValueCode(), "product.formula.optionValueCodeRequired"));
            entity.setValueNameCn(requiredTrim(entity.getValueNameCn(), "product.formula.optionValueNameRequired"));
            entity.setValueNameEn(trim(entity.getValueNameEn()));
            if (!keys.add(key(entity.getOptionCode(), entity.getValueCode()))) {
                throw ServiceException.ofMessageKey("product.formula.optionValueDuplicate");
            }
            entity.setDefaultFlag(Boolean.TRUE.equals(entity.getDefaultFlag()));
            entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
            entity.setDelFlag("0");
            entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
            result.add(entity);
            index++;
        }
        return result;
    }

    void normalizeOptionVisibility(List<ProductFormulaOption> options, List<ProductFormulaOptionValue> values) {
        ProductFormulaOptionRefResolver resolver = new ProductFormulaOptionRefResolver(options, values);
        for (ProductFormulaOption option : options) {
            applyDefaultValue(option, resolver);
            if (!VISIBILITY_CONDITIONAL.equals(option.getVisibilityMode())) {
                option.setVisibilityMode(VISIBILITY_ALWAYS);
                clearOptionVisibilityCondition(option);
                continue;
            }
            applyVisibilityCondition(option, resolver);
        }
    }

    private void applyDefaultValue(ProductFormulaOption option, ProductFormulaOptionRefResolver resolver) {
        if (StringUtils.isBlank(option.getDefaultValueRefKey()) && StringUtils.isBlank(option.getDefaultValueCode())) {
            option.setDefaultValueNameCn(null);
            return;
        }
        ProductFormulaOptionValue defaultValue = resolver.value(option, option.getDefaultValueRefKey(), option.getDefaultValueCode());
        if (defaultValue == null) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityValueInvalid");
        }
        option.setDefaultValueRefKey(defaultValue.getValueRefKey());
        option.setDefaultValueCode(defaultValue.getValueCode());
        option.setDefaultValueNameCn(defaultValue.getValueNameCn());
    }

    private void applyVisibilityCondition(ProductFormulaOption option, ProductFormulaOptionRefResolver resolver) {
        if (StringUtils.isBlank(option.getVisibleConditionOptionRefKey()) && StringUtils.isBlank(option.getVisibleConditionOptionCode())) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityConditionRequired");
        }
        ProductFormulaOption conditionOption =
            resolver.option(option.getVisibleConditionOptionRefKey(), option.getVisibleConditionOptionCode());
        if (conditionOption == null) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityConditionInvalid");
        }
        if (conditionOption.getOptionRefKey().equals(option.getOptionRefKey())) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilitySelfDenied");
        }
        if (StringUtils.isBlank(option.getVisibleConditionValueRefKey()) && StringUtils.isBlank(option.getVisibleConditionValueCode())) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityValueRequired");
        }
        ProductFormulaOptionValue conditionValue =
            resolver.value(conditionOption, option.getVisibleConditionValueRefKey(), option.getVisibleConditionValueCode());
        if (conditionValue == null) {
            throw ServiceException.ofMessageKey("product.formula.optionVisibilityValueInvalid");
        }
        option.setVisibleConditionOptionRefKey(conditionOption.getOptionRefKey());
        option.setVisibleConditionOptionCode(conditionOption.getOptionCode());
        option.setVisibleConditionOptionNameCn(conditionOption.getOptionNameCn());
        option.setVisibleConditionValueRefKey(conditionValue.getValueRefKey());
        option.setVisibleConditionValueCode(conditionValue.getValueCode());
        option.setVisibleConditionValueNameCn(conditionValue.getValueNameCn());
    }

    private void clearOptionVisibilityCondition(ProductFormulaOption option) {
        option.setVisibleConditionOptionRefKey(null);
        option.setVisibleConditionOptionCode(null);
        option.setVisibleConditionOptionNameCn(null);
        option.setVisibleConditionValueRefKey(null);
        option.setVisibleConditionValueCode(null);
        option.setVisibleConditionValueNameCn(null);
    }

    private String requiredTrim(String value, String messageKey) {
        String trimmed = trim(value);
        if (StringUtils.isBlank(trimmed)) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return trimmed;
    }

    private String requiredUpper(String value, String messageKey) {
        String trimmed = trimUpper(value);
        if (StringUtils.isBlank(trimmed)) {
            throw ServiceException.ofMessageKey(messageKey);
        }
        return trimmed;
    }

    private String trimUpper(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toUpperCase(java.util.Locale.ROOT);
    }

    private String trim(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String defaultString(String value, String defaultValue) {
        return StringUtils.isBlank(value) ? defaultValue : value;
    }

    private String normalizeDisplayMode(String value) {
        String mode = trimUpper(value);
        return DISPLAY_IMAGE_SELECT.equals(mode) ? DISPLAY_IMAGE_SELECT : DISPLAY_SELECT;
    }

    private String normalizeHelpType(String value) {
        String type = trimUpper(value);
        return HELP_TEXT.equals(type) || HELP_RICH_TEXT.equals(type) ? HELP_TEXT : HELP_LINK;
    }

    private String stripHtml(String value) {
        String content = trim(value);
        return content == null ? null : content.replaceAll("<[^>]*>", "");
    }

    private String key(String... parts) {
        return String.join("|", parts);
    }
}
