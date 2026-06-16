package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.EngineeringItem;
import com.bocoo.product.domain.entity.EngineeringItemScope;
import com.bocoo.product.domain.entity.EngineeringOutputRule;
import com.bocoo.product.domain.entity.EngineeringRule;
import com.bocoo.product.service.EngineeringPreviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EngineeringPreviewServiceImpl implements EngineeringPreviewService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EngineeringDataReader dataReader;

    @Override
    public Map<String, Object> preview(Map<String, Object> input) {
        Long versionId = numberValue(input.get("versionId"));
        if (versionId == null) {
            versionId = numberValue(input.get("engineeringPlanVersionId"));
        }
        Map<String, Object> result = baseResult(versionId);
        if (versionId == null) {
            addMessage(result, "blockers", null, "必须选择工程方案版本", "Engineering plan version is required");
            result.put("resultStatus", "BLOCKER");
            return result;
        }

        List<EngineeringItem> items = dataReader.items(versionId);
        List<EngineeringItemScope> scopes = dataReader.scopes(versionId);
        Map<String, Object> selected = selectedItems(input);
        Map<String, List<Map<String, Object>>> available = new LinkedHashMap<>();
        for (EngineeringItem item : items) {
            List<Map<String, Object>> options = dataReader.resolveOptions(item, scopes);
            available.put(item.getItemCode(), options);
            if ("1".equals(item.getRequiredFlag()) && !selected.containsKey(item.getItemCode()) && StringUtils.isBlank(item.getDefaultSourceCode())) {
                addMessage(result, "missingItems", item.getItemCode(), item.getItemNameCn(), item.getItemNameEn());
            }
        }
        result.put("resolvedItems", items);
        result.put("availableOptions", available);

        for (EngineeringRule rule : dataReader.rules(versionId)) {
            if (!conditionMatches(rule.getConditionJson(), input, selected)) {
                continue;
            }
            resultList(result, "matchedRules").add(ruleRow(rule));
            applyRuleAction(result, rule);
        }
        for (EngineeringOutputRule outputRule : dataReader.outputRules(versionId)) {
            if (!conditionMatches(outputRule.getConditionJson(), input, selected)) {
                continue;
            }
            resultList(result, "matchedRules").add(outputRuleRow(outputRule));
            String key = switch (StringUtils.blankToDefault(outputRule.getOutputType(), "")) {
                case "COMPONENT" -> "outputComponents";
                case "MEDIA" -> "outputMediaAssets";
                default -> "outputMaterials";
            };
            resultList(result, key).add(outputRow(outputRule));
        }
        if (!resultList(result, "blockers").isEmpty()) {
            result.put("resultStatus", "BLOCKER");
        } else if (!resultList(result, "warnings").isEmpty() || !resultList(result, "disabledOptions").isEmpty()) {
            result.put("resultStatus", "WARNING");
        }
        return result;
    }

    private boolean conditionMatches(String conditionJson, Map<String, Object> input, Map<String, Object> selected) {
        if (StringUtils.isBlank(conditionJson)) {
            return true;
        }
        Object all = parseMap(conditionJson).get("all");
        if (!(all instanceof List<?> clauses)) {
            return true;
        }
        for (Object clause : clauses) {
            if (!(clause instanceof Map<?, ?> map) || !matchesClause(map, input, selected)) {
                return false;
            }
        }
        return true;
    }

    private boolean matchesClause(Map<?, ?> clause, Map<String, Object> input, Map<String, Object> selected) {
        Object left = resolveOperand(String.valueOf(clause.get("left")), input, selected);
        Object right = clause.get("right");
        String op = StringUtils.blankToDefault(String.valueOf(clause.get("op")), "EQ");
        BigDecimal leftNumber = decimalValue(left);
        BigDecimal rightNumber = decimalValue(right);
        return switch (op) {
            case "GT" -> leftNumber != null && rightNumber != null && leftNumber.compareTo(rightNumber) > 0;
            case "GTE" -> leftNumber != null && rightNumber != null && leftNumber.compareTo(rightNumber) >= 0;
            case "LT" -> leftNumber != null && rightNumber != null && leftNumber.compareTo(rightNumber) < 0;
            case "LTE" -> leftNumber != null && rightNumber != null && leftNumber.compareTo(rightNumber) <= 0;
            case "NE" -> !Objects.equals(String.valueOf(left), String.valueOf(right));
            default -> Objects.equals(String.valueOf(left), String.valueOf(right));
        };
    }

    private Object resolveOperand(String operand, Map<String, Object> input, Map<String, Object> selected) {
        if (operand == null) {
            return null;
        }
        if (operand.startsWith("input.")) {
            return input.get(operand.substring("input.".length()));
        }
        if (operand.startsWith("selected.")) {
            return selected.get(operand.substring("selected.".length()));
        }
        return operand;
    }

    private void applyRuleAction(Map<String, Object> result, EngineeringRule rule) {
        Map<String, Object> action = parseMap(rule.getActionJson());
        String actionType = StringUtils.blankToDefault(String.valueOf(action.get("type")), "");
        if ("DISABLE_OPTION".equals(actionType)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("itemCode", action.get("itemCode"));
            row.put("scopeCode", action.get("scopeCode"));
            row.put("ruleCode", rule.getRuleCode());
            row.put("reasonCn", rule.getMessageCn());
            row.put("reasonEn", rule.getMessageEn());
            resultList(result, "disabledOptions").add(row);
            addMessage(result, "warnings", rule.getRuleCode(), rule.getMessageCn(), rule.getMessageEn());
        } else if ("BLOCK".equals(actionType) || "BLOCKER".equals(rule.getSeverity())) {
            addMessage(result, "blockers", rule.getRuleCode(), rule.getMessageCn(), rule.getMessageEn());
        } else if ("REQUIRE_ITEM".equals(actionType)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("itemCode", action.get("itemCode"));
            row.put("ruleCode", rule.getRuleCode());
            row.put("textCn", rule.getMessageCn());
            row.put("textEn", rule.getMessageEn());
            resultList(result, "missingItems").add(row);
        } else if ("MEDIA_HINT".equals(actionType)) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("outputType", "MEDIA");
            row.put("outputCode", action.get("assetCode"));
            row.put("outputNameCn", action.get("assetNameCn"));
            row.put("outputNameEn", action.get("assetNameEn"));
            row.put("defaultQty", BigDecimal.ONE);
            row.put("unitCode", "PCS");
            row.put("reasonCn", rule.getMessageCn());
            row.put("reasonEn", rule.getMessageEn());
            resultList(result, "outputMediaAssets").add(row);
            addMessage(result, "warnings", rule.getRuleCode(), rule.getMessageCn(), rule.getMessageEn());
        } else {
            addMessage(result, "warnings", rule.getRuleCode(), rule.getMessageCn(), rule.getMessageEn());
        }
    }

    private Map<String, Object> baseResult(Long versionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("versionId", versionId);
        result.put("resultStatus", "PASS");
        result.put("resolvedItems", List.of());
        result.put("availableOptions", Map.of());
        result.put("disabledOptions", new ArrayList<Map<String, Object>>());
        result.put("outputComponents", new ArrayList<Map<String, Object>>());
        result.put("outputMaterials", new ArrayList<Map<String, Object>>());
        result.put("outputMediaAssets", new ArrayList<Map<String, Object>>());
        result.put("warnings", new ArrayList<Map<String, Object>>());
        result.put("blockers", new ArrayList<Map<String, Object>>());
        result.put("missingItems", new ArrayList<Map<String, Object>>());
        result.put("matchedRules", new ArrayList<Map<String, Object>>());
        return result;
    }

    private Map<String, Object> ruleRow(EngineeringRule rule) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("type", "RULE");
        row.put("ruleCode", rule.getRuleCode());
        row.put("ruleNameCn", rule.getRuleNameCn());
        row.put("severity", rule.getSeverity());
        return row;
    }

    private Map<String, Object> outputRuleRow(EngineeringOutputRule rule) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("type", "OUTPUT_RULE");
        row.put("ruleCode", rule.getRuleCode());
        row.put("ruleNameCn", rule.getRuleNameCn());
        row.put("outputCode", rule.getOutputCode());
        return row;
    }

    private Map<String, Object> outputRow(EngineeringOutputRule rule) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("outputType", rule.getOutputType());
        row.put("outputCode", rule.getOutputCode());
        row.put("outputNameCn", rule.getOutputNameCn());
        row.put("outputNameEn", rule.getOutputNameEn());
        row.put("defaultQty", rule.getDefaultQty());
        row.put("unitCode", rule.getUnitCode());
        row.put("reasonCn", rule.getReasonCn());
        row.put("reasonEn", rule.getReasonEn());
        return row;
    }

    private void addMessage(Map<String, Object> result, String key, String code, String textCn, String textEn) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("code", code);
        row.put("textCn", textCn);
        row.put("textEn", textEn);
        resultList(result, key).add(row);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> resultList(Map<String, Object> result, String key) {
        return (List<Map<String, Object>>) result.computeIfAbsent(key, ignored -> new ArrayList<Map<String, Object>>());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> selectedItems(Map<String, Object> input) {
        Object selected = input.get("selectedItems");
        if (selected == null) {
            selected = input.get("selected");
        }
        return selected instanceof Map<?, ?> map ? (Map<String, Object>) map : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseMap(String json) {
        if (StringUtils.isBlank(json)) {
            return new LinkedHashMap<>();
        }
        try {
            return OBJECT_MAPPER.readValue(json, Map.class);
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
    }

    private Long numberValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal decimalValue(Object value) {
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
