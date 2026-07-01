package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductFormulaUsageRule;
import com.bocoo.product.mapper.ProductFormulaMaterialMapper;
import com.bocoo.product.mapper.ProductFormulaOptionMapper;
import com.bocoo.product.mapper.ProductFormulaOptionMaterialMapper;
import com.bocoo.product.mapper.ProductFormulaOptionValueMapper;
import com.bocoo.product.mapper.ProductFormulaRestrictionMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductFormulaUsageRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFormulaSetupWriter extends ProductServiceSupport {

    private final ProductFormulaMaterialMapper materialMapper;
    private final ProductFormulaOptionMapper optionMapper;
    private final ProductFormulaOptionValueMapper optionValueMapper;
    private final ProductFormulaOptionMaterialMapper optionMaterialMapper;
    private final ProductFormulaRestrictionMapper restrictionMapper;
    private final ProductFormulaUsageRuleService usageRuleService;

    void replace(Long formulaId, ProductFormulaSetupRows rows) {
        deleteByFormula(formulaId);
        insertAll(rows.materials(), materialMapper);
        Map<String, Long> optionIds = insertOptions(rows.options());
        Map<String, Long> valueIds = insertValues(rows.values(), optionIds);
        assignGeneratedIds(rows.optionMaterials(), rows.materials(), optionIds, valueIds);
        insertAll(rows.optionMaterials(), optionMaterialMapper);
        insertAll(rows.restrictions(), restrictionMapper);
        usageRuleService.insertAll(rows.usageRules(), rows.materials());
    }

    void replaceMaterials(Long formulaId, ProductFormulaSetupRows rows) {
        materialMapper.delete(activeQuery(ProductFormulaMaterial.class).eq("formula_id", formulaId));
        optionMaterialMapper.delete(activeQuery(ProductFormulaOptionMaterial.class).eq("formula_id", formulaId));
        usageRuleService.deleteByFormula(formulaId);
        insertAll(rows.materials(), materialMapper);
        refreshFormulaMaterialIds(rows.optionMaterials(), rows.materials());
        insertAll(rows.optionMaterials(), optionMaterialMapper);
        usageRuleService.insertAll(rows.usageRules(), rows.materials());
    }

    void replaceOptions(Long formulaId, ProductFormulaSetupRows rows) {
        optionMapper.delete(activeQuery(ProductFormulaOption.class).eq("formula_id", formulaId));
        optionValueMapper.delete(activeQuery(ProductFormulaOptionValue.class).eq("formula_id", formulaId));
        optionMaterialMapper.delete(activeQuery(ProductFormulaOptionMaterial.class).eq("formula_id", formulaId));
        restrictionMapper.delete(activeQuery(ProductFormulaRestriction.class).eq("formula_id", formulaId));
        Map<String, Long> optionIds = insertOptions(rows.options());
        Map<String, Long> valueIds = insertValues(rows.values(), optionIds);
        assignGeneratedIds(rows.optionMaterials(), rows.materials(), optionIds, valueIds);
        insertAll(rows.optionMaterials(), optionMaterialMapper);
        insertAll(rows.restrictions(), restrictionMapper);
    }

    private void deleteByFormula(Long formulaId) {
        materialMapper.delete(activeQuery(ProductFormulaMaterial.class).eq("formula_id", formulaId));
        optionMapper.delete(activeQuery(ProductFormulaOption.class).eq("formula_id", formulaId));
        optionValueMapper.delete(activeQuery(ProductFormulaOptionValue.class).eq("formula_id", formulaId));
        optionMaterialMapper.delete(activeQuery(ProductFormulaOptionMaterial.class).eq("formula_id", formulaId));
        restrictionMapper.delete(activeQuery(ProductFormulaRestriction.class).eq("formula_id", formulaId));
        usageRuleService.deleteByFormula(formulaId);
    }

    private <T> void insertAll(List<T> rows, BaseMapper<T> mapper) {
        for (T row : rows) {
            if (row instanceof BaseEntity entity) {
                ProductEntityDefaults.prepareInsert(entity);
            }
            mapper.insert(row);
        }
    }

    private Map<String, Long> insertOptions(List<ProductFormulaOption> options) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (ProductFormulaOption option : options) {
            ProductEntityDefaults.prepareInsert(option);
            optionMapper.insert(option);
            ids.put(option.getOptionCode(), option.getOptionId());
        }
        return ids;
    }

    private Map<String, Long> insertValues(List<ProductFormulaOptionValue> values, Map<String, Long> optionIds) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (ProductFormulaOptionValue value : values) {
            value.setOptionId(optionIds.get(value.getOptionCode()));
            ProductEntityDefaults.prepareInsert(value);
            optionValueMapper.insert(value);
            ids.put(key(value.getOptionCode(), value.getValueCode()), value.getOptionValueId());
        }
        return ids;
    }

    private void assignGeneratedIds(List<ProductFormulaOptionMaterial> optionMaterials, List<ProductFormulaMaterial> materials,
                                    Map<String, Long> optionIds, Map<String, Long> valueIds) {
        Map<String, Long> materialIds = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, ProductFormulaMaterial::getFormulaMaterialId, (left, right) -> left));
        for (ProductFormulaOptionMaterial optionMaterial : optionMaterials) {
            optionMaterial.setOptionId(optionIds.get(optionMaterial.getOptionCode()));
            optionMaterial.setOptionValueId(valueIds.get(key(optionMaterial.getOptionCode(), optionMaterial.getValueCode())));
            optionMaterial.setFormulaMaterialId(materialIds.get(optionMaterial.getMaterialCode()));
        }
    }

    private void refreshFormulaMaterialIds(List<ProductFormulaOptionMaterial> optionMaterials, List<ProductFormulaMaterial> materials) {
        Map<String, ProductFormulaMaterial> materialMap = materials.stream()
            .collect(Collectors.toMap(ProductFormulaMaterial::getMaterialCode, material -> material, (left, right) -> left));
        for (ProductFormulaOptionMaterial optionMaterial : optionMaterials) {
            ProductFormulaMaterial material = materialMap.get(optionMaterial.getMaterialCode());
            if (material == null) {
                continue;
            }
            optionMaterial.setFormulaMaterialId(material.getFormulaMaterialId());
            optionMaterial.setMaterialId(material.getMaterialId());
            optionMaterial.setMaterialNameCn(material.getMaterialNameCn());
        }
    }

    private String key(String... parts) {
        return String.join("|", parts);
    }
}
