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
        entity.setTargetOptionCode(defaultString(trimUpper(entity.getTargetOptionCode()), ""));
        entity.setConditionType(requiredUpper(entity.getConditionType(), "product.formula.restrictionConditionRequired"));
        entity.setConditionOptionCode(trimUpper(entity.getConditionOptionCode()));
        entity.setConditionOperator(defaultString(trimUpper(entity.getConditionOperator()), "EXPRESSION"));
        entity.setConditionValueCode(trimUpper(entity.getConditionValueCode()));
        entity.setConditionExpression(trim(entity.getConditionExpression()));
        entity.setConditionText(defaultString(trim(entity.getConditionText()), entity.getConditionExpression()));
        entity.setActionType(requiredUpper(entity.getActionType(), "product.formula.restrictionActionRequired"));
        entity.setTargetValueCode(trimUpper(entity.getTargetValueCode()));
        entity.setMessageText(trim(entity.getMessageText()));
        entity.setStatus(defaultString(trim(entity.getStatus()), STATUS_ENABLED));
        entity.setDelFlag("0");
        entity.setSortOrder(entity.getSortOrder() == null ? index * 10 + 10 : entity.getSortOrder());
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
