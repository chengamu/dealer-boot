package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.ProductPriceMaterial;
import com.bocoo.product.domain.entity.ProductPriceMaterialRule;
import com.bocoo.product.domain.vo.ProductPriceMaterialVo;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductPriceMaterialVoAssembler {

    List<ProductPriceMaterialVo> toMaterialVos(List<ProductPriceMaterial> materials, List<ProductPriceMaterialRule> rules) {
        Map<Long, List<ProductPriceMaterialRule>> rulesByMaterial = rules.stream()
            .collect(Collectors.groupingBy(ProductPriceMaterialRule::getPriceMaterialId));
        return materials.stream().map(material -> {
            ProductPriceMaterialVo vo = new ProductPriceMaterialVo();
            vo.setPriceMaterialId(material.getPriceMaterialId());
            vo.setTenantId(material.getTenantId());
            vo.setPriceSettingId(material.getPriceSettingId());
            vo.setSaleProductId(material.getSaleProductId());
            vo.setFormulaVersionId(material.getFormulaVersionId());
            vo.setFormulaMaterialId(material.getFormulaMaterialId());
            vo.setMaterialId(material.getMaterialId());
            vo.setMaterialCode(material.getMaterialCode());
            vo.setMaterialNameCn(material.getMaterialNameCn());
            vo.setSpecModelText(material.getSpecModelText());
            vo.setAttributeGroupCode(material.getAttributeGroupCode());
            vo.setAttributeGroupNameCn(material.getAttributeGroupNameCn());
            vo.setMaterialTypeCode(material.getMaterialTypeCode());
            vo.setMaterialTypeNameCn(material.getMaterialTypeNameCn());
            vo.setUnitCode(material.getUnitCode());
            vo.setStatus(material.getStatus());
            vo.setSortOrder(material.getSortOrder());
            vo.setRemark(material.getRemark());
            List<ProductPriceMaterialRule> materialRules = rulesByMaterial.getOrDefault(material.getPriceMaterialId(), List.of());
            vo.setRuleCount(materialRules.size());
            vo.setDefaultRuleFlag(materialRules.stream().anyMatch(rule -> Boolean.TRUE.equals(rule.getDefaultRuleFlag())));
            return vo;
        }).toList();
    }
}
