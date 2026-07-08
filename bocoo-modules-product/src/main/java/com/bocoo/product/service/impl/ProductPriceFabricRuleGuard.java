package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.bo.ProductPriceFabricRuleBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductPriceOptionCombinationVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductPriceFabricRuleGuard {

    private final ProductPriceSnapshotReader snapshotReader;

    public void assertSavable(ProductFormulaVersion version, List<ProductPriceFabricRuleBo> rules) {
        Set<String> allowedKeys = allowedKeys(version);
        Set<String> seenKeys = new HashSet<>();
        for (ProductPriceFabricRuleBo rule : rules == null ? List.<ProductPriceFabricRuleBo>of() : rules) {
            String key = key(rule.getMaterialCode(), rule.getOptionCombinationKey());
            if (!seenKeys.add(key)) {
                throw ServiceException.ofMessageKey("product.priceSetting.duplicateFabricPrice");
            }
            if (!allowedKeys.contains(key)) {
                throw ServiceException.ofMessageKey("product.priceSetting.fabricPriceNotInFormulaVersion");
            }
        }
    }

    private Set<String> allowedKeys(ProductFormulaVersion version) {
        List<ProductPriceOptionCombinationVo> combinations = snapshotReader.optionCombinations(version);
        return snapshotReader.fabricMaterials(version).stream()
            .map(material -> String.valueOf(material.get("materialCode")))
            .flatMap(materialCode -> combinations.stream()
                .map(combo -> key(materialCode, combo.getOptionCombinationKey())))
            .collect(Collectors.toSet());
    }

    private String key(String materialCode, String optionCombinationKey) {
        return StringUtils.blankToDefault(materialCode, "") + "||"
            + StringUtils.blankToDefault(optionCombinationKey, "DEFAULT");
    }
}
