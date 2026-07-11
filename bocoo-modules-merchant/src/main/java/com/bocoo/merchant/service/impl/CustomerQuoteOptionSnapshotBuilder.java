package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.vo.ProductFormulaMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionMaterialVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionValueVo;
import com.bocoo.product.domain.vo.ProductFormulaOptionVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
class CustomerQuoteOptionSnapshotBuilder {

    private final CustomerQuoteOptionLabelResolver labelResolver;

    CustomerQuoteOptionSnapshot build(ProductPriceSetupVo setup, Map<String, String> selections) {
        Map<String, String> selected = selections == null ? Map.of() : new LinkedHashMap<>(selections);
        List<ProductFormulaOptionVo> options = sorted(setup.getFormulaOptions());
        List<String> cnParts = new ArrayList<>();
        List<String> enParts = new ArrayList<>();
        boolean englishComplete = true;
        boolean motorized = false;
        for (ProductFormulaOptionVo option : options) {
            String valueCode = labelResolver.selectedValue(selected, option);
            if (StringUtils.isBlank(valueCode)) {
                continue;
            }
            ProductFormulaOptionValueVo value = findValue(setup.getFormulaOptionValues(), option, valueCode);
            cnParts.add(labelResolver.displayCn(option, value, valueCode));
            String en = labelResolver.displayEn(option, value);
            if (StringUtils.isBlank(en)) {
                englishComplete = false;
                enParts.add(option.getOptionCode() + "=" + valueCode);
            } else {
                enParts.add(en);
            }
            motorized = motorized || isMotorized(setup, option, valueCode);
        }
        return new CustomerQuoteOptionSnapshot(selected, String.join(" / ", cnParts),
            String.join(" / ", enParts), englishComplete, motorized);
    }

    private List<ProductFormulaOptionVo> sorted(List<ProductFormulaOptionVo> options) {
        if (options == null) {
            return List.of();
        }
        return options.stream()
            .filter(row -> Boolean.TRUE.equals(row.getBusinessVisibleFlag()))
            .sorted(Comparator.comparing(row -> row.getSortOrder() == null ? Integer.MAX_VALUE : row.getSortOrder()))
            .toList();
    }

    private ProductFormulaOptionValueVo findValue(List<ProductFormulaOptionValueVo> values,
                                                   ProductFormulaOptionVo option, String selectedValue) {
        if (values == null) {
            return null;
        }
        return values.stream()
            .filter(row -> matches(row.getOptionCode(), row.getOptionRefKey(), option.getOptionCode(), option.getOptionRefKey()))
            .filter(row -> selectedValue.equals(row.getValueCode()) || selectedValue.equals(row.getValueRefKey()))
            .findFirst()
            .orElse(null);
    }

    private boolean isMotorized(ProductPriceSetupVo setup, ProductFormulaOptionVo option, String selectedValue) {
        Map<Long, ProductFormulaMaterialVo> materials = new LinkedHashMap<>();
        if (setup.getFormulaMaterials() != null) {
            setup.getFormulaMaterials().forEach(row -> materials.put(row.getMaterialId(), row));
        }
        List<ProductFormulaOptionMaterialVo> links = setup.getFormulaOptionMaterials();
        if (links == null) {
            return false;
        }
        return links.stream()
            .filter(row -> matches(row.getOptionCode(), row.getOptionRefKey(), option.getOptionCode(), option.getOptionRefKey()))
            .filter(row -> selectedValue.equals(row.getValueCode()) || selectedValue.equals(row.getValueRefKey()))
            .map(row -> materials.get(row.getMaterialId()))
            .anyMatch(row -> row != null && "MOTOR".equalsIgnoreCase(row.getMaterialTypeCode()));
    }

    private boolean matches(String code, String ref, String targetCode, String targetRef) {
        return StringUtils.equals(code, targetCode) || StringUtils.equals(ref, targetRef);
    }

}
