package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

final class ProductFormulaSimulationSelections {
    private ProductFormulaSimulationSelections() {
    }

    static boolean selectedValueMatches(String selected, String expected) {
        if (StringUtils.isBlank(expected)) {
            return StringUtils.isBlank(selected);
        }
        return selectedValues(selected).contains(expected);
    }

    static Set<String> selectedValues(String selected) {
        if (StringUtils.isBlank(selected)) {
            return Set.of();
        }
        return Arrays.stream(selected.split(","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toSet());
    }
}
