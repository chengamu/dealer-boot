package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductFormulaRestrictionBo;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFormulaRestrictionNormalizer extends ProductServiceSupport {

    private final ProductFormulaSetupValidator setupValidator;

    List<ProductFormulaRestriction> normalize(Long formulaId, List<ProductFormulaOption> options,
                                              List<ProductFormulaOptionValue> values,
                                              List<ProductFormulaRestrictionBo> rows) {
        ProductFormulaOptionRefResolver resolver = new ProductFormulaOptionRefResolver(options, values);
        Set<String> optionCodes = options.stream().map(ProductFormulaOption::getOptionCode).collect(Collectors.toSet());
        Set<String> valueKeys = values.stream().map(value -> key(value.getOptionCode(), value.getValueCode())).collect(Collectors.toSet());
        List<ProductFormulaRestriction> result = new ArrayList<>();
        int index = 0;
        for (ProductFormulaRestrictionBo row : rows == null ? List.<ProductFormulaRestrictionBo>of() : rows) {
            ProductFormulaRestriction entity = MapstructUtils.convert(row, ProductFormulaRestriction.class);
            if (entity == null) {
                continue;
            }
            prepareRestriction(formulaId, entity, index);
            normalizeReferences(entity, resolver);
            String messageKey = setupValidator.validateRestriction(entity, optionCodes, valueKeys);
            if (messageKey != null) {
                throw ServiceException.ofMessageKey(messageKey);
            }
            result.add(entity);
            index++;
        }
        return result;
    }

    private void prepareRestriction(Long formulaId, ProductFormulaRestriction entity, int index) {
        entity.setFormulaId(formulaId);
        entity.setRestrictionName(defaultString(trim(entity.getRestrictionName()), "限制条件" + (index + 1)));
        entity.setTargetOptionRefKey(trim(entity.getTargetOptionRefKey()));
        entity.setTargetOptionCode(defaultString(trimUpper(entity.getTargetOptionCode()), ""));
        entity.setConditionType(requiredUpper(entity.getConditionType(), "product.formula.restrictionConditionRequired"));
        entity.setConditionOptionRefKey(trim(entity.getConditionOptionRefKey()));
        entity.setConditionOptionCode(trimUpper(entity.getConditionOptionCode()));
        entity.setConditionOperator(defaultString(trimUpper(entity.getConditionOperator()), "EXPRESSION"));
        entity.setConditionValueRefKey(trim(entity.getConditionValueRefKey()));
        entity.setConditionValueCode(trimUpper(entity.getConditionValueCode()));
        entity.setConditionExpression(trim(entity.getConditionExpression()));
        entity.setConditionText(defaultString(trim(entity.getConditionText()), entity.getConditionExpression()));
        entity.setActionType(requiredUpper(entity.getActionType(), "product.formula.restrictionActionRequired"));
        entity.setTargetValueRefKey(trim(entity.getTargetValueRefKey()));
        entity.setTargetValueCode(trimUpper(entity.getTargetValueCode()));
        entity.setMessageText(trim(entity.getMessageText()));
        entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
        entity.setDelFlag("0");
        entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
    }

    private void normalizeReferences(ProductFormulaRestriction entity, ProductFormulaOptionRefResolver resolver) {
        ProductFormulaOption targetOption = resolver.option(entity.getTargetOptionRefKey(), entity.getTargetOptionCode());
        if (targetOption != null) {
            entity.setTargetOptionRefKey(targetOption.getOptionRefKey());
            entity.setTargetOptionCode(targetOption.getOptionCode());
            ProductFormulaOptionValue targetValue = resolver.value(targetOption, entity.getTargetValueRefKey(), entity.getTargetValueCode());
            if (targetValue != null) {
                entity.setTargetValueRefKey(targetValue.getValueRefKey());
                entity.setTargetValueCode(targetValue.getValueCode());
            }
        }
        if ("OPTION_VALUE".equals(entity.getConditionType())) {
            ProductFormulaOption option = resolver.option(entity.getConditionOptionRefKey(), entity.getConditionOptionCode());
            ProductFormulaOptionValue value = resolver.value(option, entity.getConditionValueRefKey(), entity.getConditionValueCode());
            if (option != null && value != null) {
                entity.setConditionOptionRefKey(option.getOptionRefKey());
                entity.setConditionOptionCode(option.getOptionCode());
                entity.setConditionValueRefKey(value.getValueRefKey());
                entity.setConditionValueCode(value.getValueCode());
                entity.setConditionJson(ProductFormulaConditionJsonFactory.optionValue(option, value));
            }
            return;
        }
        entity.setConditionOptionRefKey(null);
        entity.setConditionValueRefKey(null);
        if ("WIDTH".equals(entity.getConditionType()) || "HEIGHT".equals(entity.getConditionType()) || "WEIGHT".equals(entity.getConditionType())) {
            entity.setConditionJson(ProductFormulaConditionJsonFactory.measure(entity.getConditionType(), entity.getConditionOperator(), entity.getConditionValueNumber()));
        } else {
            entity.setConditionJson(ProductFormulaConditionJsonFactory.expression(entity.getConditionExpression(), entity.getConditionText()));
        }
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

    private String key(String... parts) {
        return String.join("|", parts);
    }
}
