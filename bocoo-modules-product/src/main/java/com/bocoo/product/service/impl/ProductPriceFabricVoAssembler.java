package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.ProductPriceFabric;
import com.bocoo.product.domain.entity.ProductPriceFabricRule;
import com.bocoo.product.domain.vo.ProductPriceFabricVo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductPriceFabricVoAssembler {

    List<ProductPriceFabricVo> toFabricVos(List<ProductPriceFabric> fabrics, List<ProductPriceFabricRule> rules) {
        Map<Long, List<ProductPriceFabricRule>> rulesByFabric = rules.stream()
            .collect(Collectors.groupingBy(ProductPriceFabricRule::getPriceFabricId));
        return fabrics.stream().map(fabric -> {
            ProductPriceFabricVo vo = new ProductPriceFabricVo();
            vo.setPriceFabricId(fabric.getPriceFabricId());
            vo.setTenantId(fabric.getTenantId());
            vo.setPriceSettingId(fabric.getPriceSettingId());
            vo.setSaleProductId(fabric.getSaleProductId());
            vo.setFormulaVersionId(fabric.getFormulaVersionId());
            vo.setMaterialId(fabric.getMaterialId());
            vo.setMaterialCode(fabric.getMaterialCode());
            vo.setMaterialNameCn(fabric.getMaterialNameCn());
            vo.setUnitCode(fabric.getUnitCode());
            vo.setStatus(fabric.getStatus());
            vo.setSortOrder(fabric.getSortOrder());
            vo.setRemark(fabric.getRemark());
            List<ProductPriceFabricRule> fabricRules = rulesByFabric.getOrDefault(fabric.getPriceFabricId(), List.of());
            vo.setRuleCount(fabricRules.size());
            vo.setDefaultRuleFlag(fabricRules.stream().anyMatch(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag())));
            return vo;
        }).toList();
    }
}
