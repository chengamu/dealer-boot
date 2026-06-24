package com.bocoo.product.service.impl;

import com.bocoo.product.domain.entity.EngineeringCheckCase;
import com.bocoo.product.domain.entity.EngineeringItem;
import com.bocoo.product.domain.entity.EngineeringItemScope;
import com.bocoo.product.domain.entity.EngineeringOutputRule;
import com.bocoo.product.domain.entity.EngineeringRule;
import com.bocoo.product.domain.entity.FabricSeries;
import com.bocoo.product.domain.entity.ProductComponent;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.StandardSkuEngineering;
import com.bocoo.product.mapper.EngineeringCheckCaseMapper;
import com.bocoo.product.mapper.EngineeringItemMapper;
import com.bocoo.product.mapper.EngineeringItemScopeMapper;
import com.bocoo.product.mapper.EngineeringOutputRuleMapper;
import com.bocoo.product.mapper.EngineeringRuleMapper;
import com.bocoo.product.mapper.FabricSeriesMapper;
import com.bocoo.product.mapper.ProductComponentMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.StandardSkuEngineeringMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class EngineeringDataReader extends ProductServiceSupport {

    private final EngineeringItemMapper itemMapper;
    private final EngineeringItemScopeMapper scopeMapper;
    private final EngineeringRuleMapper ruleMapper;
    private final EngineeringOutputRuleMapper outputRuleMapper;
    private final StandardSkuEngineeringMapper standardSkuEngineeringMapper;
    private final EngineeringCheckCaseMapper checkCaseMapper;
    private final ProductMaterialMapper materialMapper;
    private final ProductComponentMapper componentMapper;
    private final FabricSeriesMapper fabricSeriesMapper;

    List<EngineeringItem> items(Long versionId) {
        return itemMapper.selectList(activeQuery(EngineeringItem.class).eq("version_id", versionId).orderByAsc("sort_order"));
    }

    List<EngineeringItemScope> scopes(Long versionId) {
        return scopeMapper.selectList(activeQuery(EngineeringItemScope.class).eq("version_id", versionId).orderByAsc("sort_order"));
    }

    List<EngineeringRule> rules(Long versionId) {
        return ruleMapper.selectList(activeQuery(EngineeringRule.class).eq("version_id", versionId).eq("status", "ENABLED").orderByAsc("sort_order"));
    }

    List<EngineeringOutputRule> outputRules(Long versionId) {
        return outputRuleMapper.selectList(activeQuery(EngineeringOutputRule.class).eq("version_id", versionId).eq("status", "ENABLED").orderByAsc("sort_order"));
    }

    List<StandardSkuEngineering> standardSkus(Long versionId) {
        return standardSkuEngineeringMapper.selectList(activeQuery(StandardSkuEngineering.class).eq("version_id", versionId));
    }

    List<EngineeringCheckCase> checkCases(Long versionId) {
        return checkCaseMapper.selectList(activeQuery(EngineeringCheckCase.class).eq("version_id", versionId).orderByAsc("sort_order"));
    }

    List<Map<String, Object>> resolveOptions(EngineeringItem item, List<EngineeringItemScope> allScopes) {
        List<Map<String, Object>> rows = new ArrayList<>();
        for (EngineeringItemScope scope : allScopes) {
            if (!Objects.equals(scope.getItemId(), item.getItemId()) || !"INCLUDE".equals(scope.getIncludeFlag())) {
                continue;
            }
            rows.addAll(resolveScope(scope));
        }
        return rows;
    }

    private List<Map<String, Object>> resolveScope(EngineeringItemScope scope) {
        String type = String.valueOf(scope.getScopeType());
        String code = scope.getScopeCode();
        if ("MATERIAL_TYPE".equals(type)) {
            return materialMapper.selectList(activeQuery(ProductMaterial.class).eq("material_type_code", code).eq("status", "ENABLED")).stream().map(this::materialOption).toList();
        }
        if ("MATERIAL_CODE".equals(type)) {
            return materialMapper.selectList(activeQuery(ProductMaterial.class).eq("material_code", code).eq("status", "ENABLED")).stream().map(this::materialOption).toList();
        }
        if ("COMPONENT_TYPE".equals(type)) {
            return componentMapper.selectList(activeQuery(ProductComponent.class).eq("component_type", code).eq("status", "ENABLED")).stream().map(this::componentOption).toList();
        }
        if ("COMPONENT_CODE".equals(type)) {
            return componentMapper.selectList(activeQuery(ProductComponent.class).eq("component_code", code).eq("status", "ENABLED")).stream().map(this::componentOption).toList();
        }
        if ("FABRIC_SERIES".equals(type)) {
            return fabricSeriesMapper.selectList(activeQuery(FabricSeries.class).eq("series_code", code).eq("status", "ENABLED")).stream().map(this::seriesOption).toList();
        }
        return List.of(scopeOption(scope));
    }

    private Map<String, Object> materialOption(ProductMaterial row) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("code", row.getMaterialCode());
        option.put("nameCn", row.getMaterialNameCn());
        option.put("nameEn", row.getMaterialNameEn());
        option.put("sourceType", "MATERIAL");
        option.put("materialType", row.getMaterialTypeCode());
        option.put("specModelText", row.getSpecModelText());
        return option;
    }

    private Map<String, Object> componentOption(ProductComponent row) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("code", row.getComponentCode());
        option.put("nameCn", row.getComponentNameCn());
        option.put("nameEn", row.getComponentNameEn());
        option.put("sourceType", "COMPONENT");
        option.put("componentType", row.getComponentType());
        return option;
    }

    private Map<String, Object> seriesOption(FabricSeries row) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("code", row.getSeriesCode());
        option.put("nameCn", row.getSeriesNameCn());
        option.put("nameEn", row.getSeriesNameEn());
        option.put("sourceType", "FABRIC_SERIES");
        return option;
    }

    private Map<String, Object> scopeOption(EngineeringItemScope row) {
        Map<String, Object> option = new LinkedHashMap<>();
        option.put("code", row.getScopeCode());
        option.put("nameCn", row.getScopeNameCn());
        option.put("nameEn", row.getScopeNameEn());
        option.put("sourceType", row.getScopeType());
        return option;
    }
}
