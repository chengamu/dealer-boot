package com.bocoo.product.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaMaterial;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductUnit;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductUnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFormulaMaterialSnapshotResolver extends ProductServiceSupport {

    private final ProductMaterialMapper productMaterialMapper;
    private final ProductUnitMapper unitMapper;

    void resolve(List<ProductFormulaMaterial> rows) {
        Map<Long, ProductMaterial> materialById = materialById(rows);
        Map<String, ProductMaterial> materialByCode = materialByCode(rows);
        for (ProductFormulaMaterial row : rows) {
            ProductMaterial material = findMaterial(row, materialById, materialByCode);
            if (material == null || !"0".equals(material.getDelFlag()) || !STATUS_ENABLED.equals(material.getStatus())) {
                throw ServiceException.ofMessageKey("product.formula.materialNotFound");
            }
            applySnapshot(row, material);
        }
        Set<String> enabledUnitCodes = enabledUnitCodes(rows);
        for (ProductFormulaMaterial row : rows) {
            validateUnit(row.getUnitCode(), enabledUnitCodes);
        }
    }

    private Map<Long, ProductMaterial> materialById(List<ProductFormulaMaterial> rows) {
        Set<Long> ids = rows.stream()
            .map(ProductFormulaMaterial::getMaterialId)
            .filter(id -> id != null)
            .collect(Collectors.toCollection(HashSet::new));
        if (ids.isEmpty()) {
            return Map.of();
        }
        return productMaterialMapper.selectList(activeQuery(ProductMaterial.class).in("material_id", ids)).stream()
            .collect(Collectors.toMap(ProductMaterial::getMaterialId, Function.identity(), (left, right) -> left));
    }

    private Map<String, ProductMaterial> materialByCode(List<ProductFormulaMaterial> rows) {
        Set<String> codes = rows.stream()
            .map(ProductFormulaMaterial::getMaterialCode)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(HashSet::new));
        if (codes.isEmpty()) {
            return Map.of();
        }
        return productMaterialMapper.selectList(activeQuery(ProductMaterial.class).in("material_code", codes)).stream()
            .collect(Collectors.toMap(ProductMaterial::getMaterialCode, Function.identity(), (left, right) -> left));
    }

    private ProductMaterial findMaterial(ProductFormulaMaterial row, Map<Long, ProductMaterial> materialById,
                                         Map<String, ProductMaterial> materialByCode) {
        ProductMaterial material = row.getMaterialId() == null ? null : materialById.get(row.getMaterialId());
        if (material == null && StringUtils.isNotBlank(row.getMaterialCode())) {
            material = materialByCode.get(row.getMaterialCode());
        }
        return material;
    }

    private void applySnapshot(ProductFormulaMaterial row, ProductMaterial material) {
        row.setMaterialId(material.getMaterialId());
        row.setMaterialCode(material.getMaterialCode());
        row.setMaterialNameCn(material.getMaterialNameCn());
        row.setSpecModelText(material.getSpecModelText());
        row.setAttributeGroupId(material.getAttributeGroupId());
        row.setAttributeGroupCode(material.getAttributeGroupCode());
        row.setAttributeGroupNameCn(material.getAttributeGroupNameCn());
        row.setMaterialTypeId(material.getMaterialTypeId());
        row.setMaterialTypeCode(material.getMaterialTypeCode());
        row.setMaterialTypeNameCn(material.getMaterialTypeNameCn());
        if (StringUtils.isBlank(row.getUnitCode())) {
            row.setUnitCode(material.getUnitCode());
        }
        if (StringUtils.isBlank(row.getCalculationUnitCode())) {
            row.setCalculationUnitCode(row.getUnitCode());
        }
    }

    private Set<String> enabledUnitCodes(List<ProductFormulaMaterial> rows) {
        Set<String> unitCodes = rows.stream()
            .map(ProductFormulaMaterial::getUnitCode)
            .filter(StringUtils::isNotBlank)
            .collect(Collectors.toCollection(HashSet::new));
        if (unitCodes.isEmpty()) {
            return Set.of();
        }
        return unitMapper.selectList(activeQuery(ProductUnit.class)
                .in("unit_code", unitCodes)
                .eq("status", STATUS_ENABLED))
            .stream()
            .map(ProductUnit::getUnitCode)
            .collect(Collectors.toSet());
    }

    private void validateUnit(String unitCode, Set<String> enabledUnitCodes) {
        if (StringUtils.isBlank(unitCode)) {
            throw ServiceException.ofMessageKey("product.formula.materialUnitRequired");
        }
        if (!enabledUnitCodes.contains(unitCode)) {
            throw ServiceException.ofMessageKey("product.formula.materialUnitNotFound");
        }
    }
}
