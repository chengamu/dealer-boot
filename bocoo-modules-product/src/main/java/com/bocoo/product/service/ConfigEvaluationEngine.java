package com.bocoo.product.service;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.product.domain.bo.ConfigEvaluationBo;
import com.bocoo.product.domain.vo.ConfigEvaluationResultVo;
import com.bocoo.product.domain.vo.ConfigOptionVo;
import com.bocoo.product.domain.vo.ConfigQuestionVo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 配置求值引擎。
 *
 * 当前阶段只执行受控的必填校验、可见项过滤和组件带出，不执行任意动态表达式。
 */
@Component
public class ConfigEvaluationEngine {

    public ConfigEvaluationResultVo evaluate(ConfigEvaluationBo bo, List<ConfigQuestionVo> questions, List<ConfigOptionVo> options) {
        if (bo == null) {
            bo = new ConfigEvaluationBo();
        }
        ConfigEvaluationResultVo result = new ConfigEvaluationResultVo();
        result.setTemplateVersionId(bo.getTemplateVersionId());
        if (bo.getTemplateVersionId() == null) {
            result.setResultStatus("BLOCKER");
            result.getBlockers().add("product.config.templateVersion.required");
            return result;
        }
        List<ConfigQuestionVo> visibleQuestions = safeList(questions).stream()
            .filter(question -> !"0".equals(question.getCustomerVisible()))
            .toList();
        result.setVisibleQuestions(visibleQuestions);
        Map<Long, ConfigQuestionVo> questionById = safeList(questions).stream()
            .filter(question -> question.getQuestionId() != null)
            .collect(Collectors.toMap(ConfigQuestionVo::getQuestionId, question -> question, (left, right) -> left));
        result.setAvailableOptions(safeList(options).stream()
            .filter(option -> questionById.containsKey(option.getQuestionId()))
            .toList());
        validateRequired(bo, visibleQuestions, result);
        result.setAutoComponents(buildAutoComponents(bo, options, questionById, result));
        if (!result.getBlockers().isEmpty()) {
            result.setResultStatus("BLOCKER");
        } else if (!result.getWarnings().isEmpty()) {
            result.setResultStatus("WARNING");
        }
        return result;
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }

    private void validateRequired(ConfigEvaluationBo bo, List<ConfigQuestionVo> questions, ConfigEvaluationResultVo result) {
        for (ConfigQuestionVo question : questions) {
            if (!"1".equals(question.getRequiredFlag())) {
                continue;
            }
            Object selected = selectedValue(bo, question);
            if (selected == null || StringUtils.isBlank(String.valueOf(selected))) {
                result.getBlockers().add("product.config.required.missing:" + question.getQuestionCode());
            }
        }
    }

    private List<Map<String, Object>> buildAutoComponents(ConfigEvaluationBo bo, List<ConfigOptionVo> options,
                                                           Map<Long, ConfigQuestionVo> questionById,
                                                           ConfigEvaluationResultVo result) {
        List<Map<String, Object>> components = new ArrayList<>();
        for (ConfigOptionVo option : safeList(options)) {
            ConfigQuestionVo question = questionById.get(option.getQuestionId());
            if (question == null || !selectedOptionMatches(bo, question, option) || StringUtils.isBlank(option.getComponentJson())) {
                continue;
            }
            components.addAll(parseComponents(question, option, result));
        }
        return components;
    }

    private boolean selectedOptionMatches(ConfigEvaluationBo bo, ConfigQuestionVo question, ConfigOptionVo option) {
        Object selected = selectedValue(bo, question);
        if (selected == null) {
            return false;
        }
        String selectedText = String.valueOf(selected);
        return Objects.equals(selectedText, option.getOptionCode()) || Objects.equals(selectedText, option.getOptionValue());
    }

    private Object selectedValue(ConfigEvaluationBo bo, ConfigQuestionVo question) {
        Map<String, Object> selectedOptions = bo.getSelectedOptions();
        if (selectedOptions == null || question == null) {
            return null;
        }
        Object byCode = selectedOptions.get(question.getQuestionCode());
        return byCode == null && question.getQuestionId() != null ? selectedOptions.get(String.valueOf(question.getQuestionId())) : byCode;
    }

    private List<Map<String, Object>> parseComponents(ConfigQuestionVo question, ConfigOptionVo option, ConfigEvaluationResultVo result) {
        try {
            Object parsed = JsonUtils.getObjectMapper().readValue(option.getComponentJson(), Object.class);
            List<Map<String, Object>> components = new ArrayList<>();
            if (parsed instanceof List<?> list) {
                for (Object item : list) {
                    components.add(componentRow(question, option, item));
                }
            } else {
                components.add(componentRow(question, option, parsed));
            }
            return components;
        } catch (Exception e) {
            result.getWarnings().add("product.config.componentJson.invalid:" + option.getOptionCode());
            return List.of();
        }
    }

    private Map<String, Object> componentRow(ConfigQuestionVo question, ConfigOptionVo option, Object component) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("questionCode", question.getQuestionCode());
        row.put("questionNameCn", question.getQuestionNameCn());
        row.put("questionNameEn", question.getQuestionNameEn());
        row.put("optionCode", option.getOptionCode());
        row.put("optionNameCn", option.getOptionNameCn());
        row.put("optionNameEn", option.getOptionNameEn());
        if (component instanceof Map<?, ?> componentMap) {
            componentMap.forEach((key, value) -> row.put(String.valueOf(key), value));
        } else {
            row.put("component", component);
        }
        return row;
    }
}
