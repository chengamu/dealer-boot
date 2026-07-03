package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOption;
import com.bocoo.product.domain.entity.ProductFormulaOptionMaterial;
import com.bocoo.product.domain.entity.ProductFormulaOptionValue;
import com.bocoo.product.domain.entity.ProductFormulaRestriction;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.mapper.ProductFormulaMapper;
import com.bocoo.product.mapper.ProductFormulaMaterialMapper;
import com.bocoo.product.mapper.ProductFormulaOptionMapper;
import com.bocoo.product.mapper.ProductFormulaOptionMaterialMapper;
import com.bocoo.product.mapper.ProductFormulaOptionValueMapper;
import com.bocoo.product.mapper.ProductFormulaRestrictionMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.service.ProductFormulaUsageRuleService;
import com.bocoo.product.service.ProductFormulaVariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFormulaSetupReader extends ProductServiceSupport {

    private final ProductFormulaMapper formulaMapper;
    private final ProductFormulaMaterialMapper materialMapper;
    private final ProductFormulaOptionMapper optionMapper;
    private final ProductFormulaOptionValueMapper optionValueMapper;
    private final ProductFormulaOptionMaterialMapper optionMaterialMapper;
    private final ProductFormulaRestrictionMapper restrictionMapper;
    private final ProductMaterialMapper productMaterialMapper;
    private final ProductFormulaUsageRuleService usageRuleService;
    private final ProductFormulaVariableService variableService;

    ProductFormulaSetupVo querySetup(Long formulaId) {
        ProductFormulaSetupVo vo = new ProductFormulaSetupVo();
        vo.setFormula(formulaMapper.selectVoById(formulaId));
        vo.setMaterials(materialMapper.selectVoList(activeQuery(ProductFormulaMaterial.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "line_no", "formula_material_id")));
        vo.setOptions(optionMapper.selectVoList(activeQuery(ProductFormulaOption.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_id")));
        vo.setOptionValues(optionValueMapper.selectVoList(activeQuery(ProductFormulaOptionValue.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_value_id")));
        vo.setOptionMaterials(optionMaterialMapper.selectVoList(activeQuery(ProductFormulaOptionMaterial.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_material_id")));
        vo.setRestrictions(restrictionMapper.selectVoList(activeQuery(ProductFormulaRestriction.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "restriction_id")));
        vo.setUsageRules(usageRuleService.queryByFormula(formulaId));
        vo.setVariables(variableService.queryVariableVos(formulaId));
        vo.setVariableRules(variableService.queryRuleVos(formulaId));
        return vo;
    }

    int materialCount(Long formulaId) {
        return activeMaterials(formulaId).size();
    }

    ProductFormulaSetupContext context(Long formulaId) {
        return new ProductFormulaSetupContext(
            activeMaterials(formulaId),
            activeOptions(formulaId),
            activeValues(formulaId),
            activeOptionMaterials(formulaId),
            activeRestrictions(formulaId),
            usageRuleService.activeRules(formulaId),
            variableService.activeVariables(formulaId),
            variableService.activeRules(formulaId)
        );
    }

    Map<String, Object> snapshot(Long formulaId) {
        ProductFormulaSetupContext context = context(formulaId);
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("materials", context.materials());
        snapshot.put("options", context.options());
        snapshot.put("optionValues", context.values());
        snapshot.put("optionMaterials", context.optionMaterials());
        snapshot.put("restrictions", context.restrictions());
        snapshot.put("usageRules", context.usageRules());
        snapshot.put("variables", context.variables());
        snapshot.put("variableRules", context.variableRules());
        snapshot.put("priceSnapshot", priceSnapshot(context.materials()));
        return snapshot;
    }

    private List<ProductFormulaMaterial> activeMaterials(Long formulaId) {
        return materialMapper.selectList(activeQuery(ProductFormulaMaterial.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "line_no", "formula_material_id"));
    }

    private List<ProductFormulaOption> activeOptions(Long formulaId) {
        return optionMapper.selectList(activeQuery(ProductFormulaOption.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_id"));
    }

    private List<ProductFormulaOptionValue> activeValues(Long formulaId) {
        return optionValueMapper.selectList(activeQuery(ProductFormulaOptionValue.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_value_id"));
    }

    private List<ProductFormulaOptionMaterial> activeOptionMaterials(Long formulaId) {
        return optionMaterialMapper.selectList(activeQuery(ProductFormulaOptionMaterial.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "option_material_id"));
    }

    private List<ProductFormulaRestriction> activeRestrictions(Long formulaId) {
        return restrictionMapper.selectList(activeQuery(ProductFormulaRestriction.class)
            .eq("formula_id", formulaId)
            .orderByAsc("sort_order", "restriction_id"));
    }

    private List<Map<String, Object>> priceSnapshot(List<ProductFormulaMaterial> materials) {
        Set<Long> materialIds = materials.stream()
            .map(ProductFormulaMaterial::getMaterialId)
            .filter(id -> id != null)
            .collect(Collectors.toSet());
        Map<Long, ProductMaterial> masterById = materialIds.isEmpty()
            ? Map.of()
            : productMaterialMapper.selectList(activeQuery(ProductMaterial.class).in("material_id", materialIds))
                .stream()
                .collect(Collectors.toMap(ProductMaterial::getMaterialId, Function.identity(), (left, right) -> left));
        return materials.stream().map(material -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("formulaMaterialId", material.getFormulaMaterialId());
            item.put("materialId", material.getMaterialId());
            item.put("materialCode", material.getMaterialCode());
            item.put("materialNameCn", material.getMaterialNameCn());
            ProductMaterial master = material.getMaterialId() == null ? null : masterById.get(material.getMaterialId());
            item.put("unitPrice", master == null ? null : master.getUnitPrice());
            item.put("salesPrice", master == null ? null : master.getSalesPrice());
            item.put("snapshotTime", TimeUtils.utcNow());
            return item;
        }).toList();
    }
}
