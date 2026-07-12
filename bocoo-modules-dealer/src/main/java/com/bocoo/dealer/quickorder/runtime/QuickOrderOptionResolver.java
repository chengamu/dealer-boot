package com.bocoo.dealer.quickorder.runtime;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.vo.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class QuickOrderOptionResolver {
    QuickOrderOptionResolution resolve(ProductPriceSetupVo setup, Map<String, String> source) {
        List<ProductFormulaOptionVo> options = visibleOptions(setup);
        Map<String, ProductFormulaOptionVo> aliases = optionAliases(options);
        Map<String, String> rawByCode = normalizeOptionKeys(source, aliases);
        Map<String, String> selected = new LinkedHashMap<>();
        List<String> cn = new ArrayList<>(), en = new ArrayList<>();
        for (ProductFormulaOptionVo option : options) {
            if (!conditionVisible(option, selected)) continue;
            List<ProductFormulaOptionValueVo> values = values(setup, option);
            String raw = defaulted(rawByCode.get(option.getOptionCode()), option);
            if (StringUtils.isBlank(raw)) {
                if (Boolean.TRUE.equals(option.getRequiredFlag())) invalid();
                continue;
            }
            List<ProductFormulaOptionValueVo> chosen = normalizeValues(raw, values);
            String codes = chosen.stream().map(ProductFormulaOptionValueVo::getValueCode)
                .collect(Collectors.joining(","));
            selected.put(option.getOptionCode(), codes);
            cn.add(label(option.getOptionNameCn(), option.getOptionCode()) + ": "
                + chosen.stream().map(v -> label(v.getValueNameCn(), v.getValueCode())).collect(Collectors.joining(", ")));
            en.add(label(option.getOptionNameEn(), option.getOptionCode()) + ": "
                + chosen.stream().map(v -> label(v.getValueNameEn(), v.getValueCode())).collect(Collectors.joining(", ")));
        }
        return new QuickOrderOptionResolution(selected, String.join(" / ", cn), String.join(" / ", en),
            motorized(setup, selected));
    }

    private List<ProductFormulaOptionVo> visibleOptions(ProductPriceSetupVo setup) {
        if (setup == null || setup.getFormulaOptions() == null) return List.of();
        return setup.getFormulaOptions().stream()
            .filter(row -> Boolean.TRUE.equals(row.getBusinessVisibleFlag()))
            .filter(row -> row.getStatus() == null || "ENABLED".equalsIgnoreCase(row.getStatus()))
            .sorted(Comparator.comparing(ProductFormulaOptionVo::getSortOrder,
                Comparator.nullsLast(Integer::compareTo))).toList();
    }

    private Map<String, ProductFormulaOptionVo> optionAliases(List<ProductFormulaOptionVo> options) {
        Map<String, ProductFormulaOptionVo> result = new LinkedHashMap<>();
        options.forEach(row -> {
            result.put(row.getOptionCode(), row);
            if (StringUtils.isNotBlank(row.getOptionRefKey())) result.put(row.getOptionRefKey(), row);
        });
        return result;
    }

    private Map<String, String> normalizeOptionKeys(Map<String, String> source,
                                                     Map<String, ProductFormulaOptionVo> aliases) {
        Map<String, String> result = new LinkedHashMap<>();
        if (source == null) return result;
        source.forEach((key, value) -> {
            ProductFormulaOptionVo option = aliases.get(key);
            if (option == null) invalid();
            result.put(option.getOptionCode(), value);
        });
        return result;
    }

    private List<ProductFormulaOptionValueVo> values(ProductPriceSetupVo setup, ProductFormulaOptionVo option) {
        if (setup.getFormulaOptionValues() == null) return List.of();
        return setup.getFormulaOptionValues().stream()
            .filter(row -> Objects.equals(row.getOptionId(), option.getOptionId())
                || Objects.equals(row.getOptionCode(), option.getOptionCode())
                || Objects.equals(row.getOptionRefKey(), option.getOptionRefKey()))
            .filter(row -> row.getStatus() == null || "ENABLED".equalsIgnoreCase(row.getStatus()))
            .toList();
    }

    private List<ProductFormulaOptionValueVo> normalizeValues(String raw,
                                                               List<ProductFormulaOptionValueVo> values) {
        Map<String, ProductFormulaOptionValueVo> aliases = new LinkedHashMap<>();
        values.forEach(row -> {
            aliases.put(row.getValueCode(), row);
            if (StringUtils.isNotBlank(row.getValueRefKey())) aliases.put(row.getValueRefKey(), row);
        });
        List<ProductFormulaOptionValueVo> result = Arrays.stream(raw.split(","))
            .map(String::trim).filter(token -> !token.isEmpty()).map(aliases::get).toList();
        if (result.isEmpty() || result.stream().anyMatch(Objects::isNull)) invalid();
        return result;
    }

    private boolean conditionVisible(ProductFormulaOptionVo option, Map<String, String> selected) {
        if (!"CONDITIONAL".equalsIgnoreCase(option.getVisibilityMode())) return true;
        String parent = StringUtils.blankToDefault(option.getVisibleConditionOptionCode(),
            option.getVisibleConditionOptionRefKey());
        String expected = StringUtils.blankToDefault(option.getVisibleConditionValueCode(),
            option.getVisibleConditionValueRefKey());
        String actual = selected.get(parent);
        return actual != null && Arrays.asList(actual.split(",")).contains(expected);
    }

    private boolean motorized(ProductPriceSetupVo setup, Map<String, String> selected) {
        Map<Long, ProductFormulaMaterialVo> materials = setup.getFormulaMaterials() == null ? Map.of()
            : setup.getFormulaMaterials().stream().collect(Collectors.toMap(ProductFormulaMaterialVo::getMaterialId,
                Function.identity(), (left, right) -> left));
        if (setup.getFormulaOptionMaterials() == null) return false;
        return setup.getFormulaOptionMaterials().stream().filter(link -> selected.entrySet().stream().anyMatch(entry ->
                Objects.equals(entry.getKey(), link.getOptionCode())
                    && Arrays.asList(entry.getValue().split(",")).contains(link.getValueCode())))
            .map(link -> materials.get(link.getMaterialId()))
            .anyMatch(row -> row != null && "MOTOR".equalsIgnoreCase(row.getMaterialTypeCode()));
    }

    private String defaulted(String value, ProductFormulaOptionVo option) {
        return StringUtils.isNotBlank(value) ? value
            : StringUtils.blankToDefault(option.getDefaultValueCode(), option.getDefaultValueRefKey());
    }

    private String label(String label, String fallback) {
        return StringUtils.blankToDefault(label, fallback);
    }

    private void invalid() {
        throw ServiceException.ofMessageKey("dealer.quickOrder.options.invalid");
    }
}
