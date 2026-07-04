package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.domain.entity.ProductFormulaVariable;
import com.bocoo.product.domain.entity.ProductFormulaVariableRule;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ProductFormulaVariableReferenceValidator {
    private static final Pattern VAR_PATTERN = Pattern.compile("\\bvar_([A-Za-z0-9_]+)\\b");

    String validationMessageKey(List<ProductFormulaVariable> variables, List<ProductFormulaVariableRule> rules,
                                List<ProductFormulaUsageRule> usageRules) {
        Map<String, String> keysByRef = variableRefMap(variables);
        Set<String> variableKeys = variables.stream().map(this::variableKey).collect(Collectors.toSet());
        Map<String, List<ProductFormulaVariableRule>> rulesByKey = rules.stream()
            .collect(Collectors.groupingBy(rule -> variableKey(rule.getVariableKey(), rule.getVariableCode())));
        for (String key : variableKeys) {
            if (rulesByKey.getOrDefault(key, List.of()).stream().noneMatch(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag()))) {
                return "product.formula.variableDefaultRuleRequired";
            }
        }
        if (!variableKeys.containsAll(referencedVariableKeys(rules, keysByRef))) return "product.formula.variableReferenceInvalid";
        if (!variableKeys.containsAll(referencedUsageVariableKeys(usageRules, keysByRef))) return "product.formula.variableReferenceInvalid";
        return hasCycle(variableKeys, rules, keysByRef) ? "product.formula.variableCycleInvalid" : null;
    }

    Set<String> referencedUsageVariableKeys(List<ProductFormulaUsageRule> rules, List<ProductFormulaVariable> variables) {
        return referencedUsageVariableKeys(rules, variableRefMap(variables));
    }

    private Set<String> referencedUsageVariableKeys(List<ProductFormulaUsageRule> rules, Map<String, String> keysByRef) {
        return rules.stream().flatMap(rule -> variableRefs(String.join(" ",
            blank(rule.getLengthFormula()), blank(rule.getWidthFormula()), blank(rule.getHeightFormula()),
            blank(rule.getWeightFormula()), blank(rule.getUsageFormula())), keysByRef).stream()).collect(Collectors.toSet());
    }

    private Set<String> referencedVariableKeys(List<ProductFormulaVariableRule> rules, Map<String, String> keysByRef) {
        return rules.stream().flatMap(rule -> variableRefs(rule.getFormulaExpression(), keysByRef).stream()).collect(Collectors.toSet());
    }

    private Set<String> variableRefs(String expression, Map<String, String> keysByRef) {
        Set<String> refs = new HashSet<>();
        var matcher = VAR_PATTERN.matcher(blank(expression));
        while (matcher.find()) refs.add(keysByRef.getOrDefault(matcher.group(1), matcher.group(1)));
        return refs;
    }

    private boolean hasCycle(Set<String> variableKeys, List<ProductFormulaVariableRule> rules, Map<String, String> keysByRef) {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        variableKeys.forEach(key -> graph.put(key, new HashSet<>()));
        rules.forEach(rule -> graph.getOrDefault(variableKey(rule.getVariableKey(), rule.getVariableCode()), new HashSet<>())
            .addAll(variableRefs(rule.getFormulaExpression(), keysByRef)));
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String key : variableKeys) {
            if (dfsCycle(key, graph, visiting, visited)) return true;
        }
        return false;
    }

    private Map<String, String> variableRefMap(List<ProductFormulaVariable> variables) {
        Map<String, String> refs = new LinkedHashMap<>();
        variables.forEach(variable -> {
            String key = variableKey(variable);
            refs.put(key, key);
            refs.put(variable.getVariableCode(), key);
        });
        return refs;
    }

    private String variableKey(ProductFormulaVariable variable) {
        return variableKey(variable.getVariableKey(), variable.getVariableCode());
    }

    private String variableKey(String variableKey, String variableCode) {
        return blank(variableKey).isBlank() ? variableCode : variableKey;
    }

    private boolean dfsCycle(String code, Map<String, Set<String>> graph, Set<String> visiting, Set<String> visited) {
        if (visited.contains(code)) return false;
        if (!visiting.add(code)) return true;
        for (String next : graph.getOrDefault(code, Set.of())) {
            if (dfsCycle(next, graph, visiting, visited)) return true;
        }
        visiting.remove(code);
        visited.add(code);
        return false;
    }

    private String blank(String value) {
        return value == null ? "" : value;
    }
}
