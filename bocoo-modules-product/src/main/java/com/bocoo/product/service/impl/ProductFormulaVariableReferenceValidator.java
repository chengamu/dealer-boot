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
        Set<String> variableCodes = variables.stream().map(ProductFormulaVariable::getVariableCode).collect(Collectors.toSet());
        Map<String, List<ProductFormulaVariableRule>> rulesByCode = rules.stream()
            .collect(Collectors.groupingBy(ProductFormulaVariableRule::getVariableCode));
        for (String code : variableCodes) {
            if (rulesByCode.getOrDefault(code, List.of()).stream().noneMatch(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag()))) {
                return "product.formula.variableDefaultRuleRequired";
            }
        }
        if (!variableCodes.containsAll(referencedVariableCodes(rules))) return "product.formula.variableReferenceInvalid";
        if (!variableCodes.containsAll(referencedUsageVariableCodes(usageRules))) return "product.formula.variableReferenceInvalid";
        return hasCycle(variableCodes, rules) ? "product.formula.variableCycleInvalid" : null;
    }

    Set<String> referencedUsageVariableCodes(List<ProductFormulaUsageRule> rules) {
        return rules.stream().flatMap(rule -> variableRefs(String.join(" ",
            blank(rule.getLengthFormula()), blank(rule.getWidthFormula()), blank(rule.getHeightFormula()),
            blank(rule.getWeightFormula()), blank(rule.getUsageFormula()))).stream()).collect(Collectors.toSet());
    }

    private Set<String> referencedVariableCodes(List<ProductFormulaVariableRule> rules) {
        return rules.stream().flatMap(rule -> variableRefs(rule.getFormulaExpression()).stream()).collect(Collectors.toSet());
    }

    private Set<String> variableRefs(String expression) {
        Set<String> refs = new HashSet<>();
        var matcher = VAR_PATTERN.matcher(blank(expression));
        while (matcher.find()) refs.add(matcher.group(1));
        return refs;
    }

    private boolean hasCycle(Set<String> variableCodes, List<ProductFormulaVariableRule> rules) {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        variableCodes.forEach(code -> graph.put(code, new HashSet<>()));
        rules.forEach(rule -> graph.getOrDefault(rule.getVariableCode(), new HashSet<>()).addAll(variableRefs(rule.getFormulaExpression())));
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String code : variableCodes) {
            if (dfsCycle(code, graph, visiting, visited)) return true;
        }
        return false;
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
