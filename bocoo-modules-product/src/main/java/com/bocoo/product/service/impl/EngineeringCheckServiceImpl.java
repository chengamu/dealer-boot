package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.EngineeringCheckCase;
import com.bocoo.product.domain.entity.EngineeringItem;
import com.bocoo.product.domain.entity.EngineeringItemScope;
import com.bocoo.product.domain.entity.EngineeringOutputRule;
import com.bocoo.product.domain.entity.EngineeringRule;
import com.bocoo.product.service.EngineeringCheckService;
import com.bocoo.product.service.EngineeringPreviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EngineeringCheckServiceImpl implements EngineeringCheckService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EngineeringDataReader dataReader;
    private final EngineeringPreviewService previewService;

    @Override
    public Map<String, Object> check(Long versionId) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("versionId", versionId);
        result.put("status", "PASS");
        result.put("issues", new ArrayList<Map<String, Object>>());

        List<EngineeringItem> items = dataReader.items(versionId);
        List<EngineeringItemScope> scopes = dataReader.scopes(versionId);
        Set<String> itemCodes = new HashSet<>();
        for (EngineeringItem item : items) {
            itemCodes.add(item.getItemCode());
            boolean hasScope = scopes.stream().anyMatch(scope -> Objects.equals(scope.getItemId(), item.getItemId()));
            if ("1".equals(item.getRequiredFlag()) && !hasScope) {
                issue(result, "BLOCKER", item.getItemCode(), "必选构成项缺少可选范围");
            }
        }
        for (EngineeringRule rule : dataReader.rules(versionId)) {
            if (StringUtils.isBlank(rule.getConditionJson()) || StringUtils.isBlank(rule.getActionJson())) {
                issue(result, "WARNING", rule.getRuleCode(), "规则条件或动作为空");
            }
            Object actionItem = parseMap(rule.getActionJson()).get("itemCode");
            if (actionItem != null && !itemCodes.contains(String.valueOf(actionItem))) {
                issue(result, "BLOCKER", rule.getRuleCode(), "规则引用了不存在的构成项：" + actionItem);
            }
        }
        for (EngineeringOutputRule outputRule : dataReader.outputRules(versionId)) {
            if (StringUtils.isBlank(outputRule.getOutputType()) || StringUtils.isBlank(outputRule.getOutputCode())) {
                issue(result, "WARNING", outputRule.getRuleCode(), "带出规则缺少带出类型或编码");
            }
        }
        for (EngineeringCheckCase checkCase : dataReader.checkCases(versionId)) {
            Map<String, Object> input = parseMap(checkCase.getInputJson());
            input.putIfAbsent("versionId", versionId);
            Map<String, Object> preview = previewService.preview(input);
            if ("BLOCKER".equals(preview.get("resultStatus"))) {
                issue(result, "BLOCKER", checkCase.getCaseCode(), "检查样例预览失败");
            }
        }
        if (!resultList(result, "issues").isEmpty()) {
            boolean hasBlocker = resultList(result, "issues").stream().anyMatch(row -> "BLOCKER".equals(row.get("severity")));
            result.put("status", hasBlocker ? "BLOCKER" : "WARNING");
        }
        return result;
    }

    private void issue(Map<String, Object> result, String severity, String code, String message) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("severity", severity);
        row.put("code", code);
        row.put("message", message);
        resultList(result, "issues").add(row);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> resultList(Map<String, Object> result, String key) {
        return (List<Map<String, Object>>) result.computeIfAbsent(key, ignored -> new ArrayList<Map<String, Object>>());
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
}
