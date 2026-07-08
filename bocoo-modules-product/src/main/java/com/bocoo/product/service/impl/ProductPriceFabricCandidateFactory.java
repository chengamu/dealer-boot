package com.bocoo.product.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductPriceFabricCandidateFactory {

    private final ProductPriceSnapshotReader snapshotReader;

    public List<ProductMaterialVo> candidates(ProductFormulaVersion version) {
        Map<String, Map<String, Object>> priceByCode = snapshotReader.priceSnapshotByMaterialCode(version);
        return snapshotReader.fabricMaterials(version).stream().map(material -> {
            ProductMaterialVo vo = new ProductMaterialVo();
            vo.setMaterialId(toLong(material.get("materialId")));
            vo.setMaterialCode(text(material.get("materialCode")));
            vo.setMaterialNameCn(text(material.get("materialNameCn")));
            vo.setMaterialNameEn(text(material.get("materialNameEn")));
            vo.setSpecModelText(text(material.get("specModelText")));
            vo.setAttributeGroupId(toLong(material.get("attributeGroupId")));
            vo.setAttributeGroupCode(text(material.get("attributeGroupCode")));
            vo.setAttributeGroupNameCn(text(material.get("attributeGroupNameCn")));
            vo.setMaterialTypeId(toLong(material.get("materialTypeId")));
            vo.setMaterialTypeCode(text(material.get("materialTypeCode")));
            vo.setMaterialTypeNameCn(text(material.get("materialTypeNameCn")));
            vo.setUnitCode(text(material.get("unitCode")));
            Map<String, Object> price = priceByCode.get(vo.getMaterialCode());
            if (price != null) {
                vo.setUnitPrice(toBigDecimal(price.get("unitPrice")));
                vo.setSalesPrice(toBigDecimal(price.get("salesPrice")));
            }
            return vo;
        }).toList();
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = text(value);
        return StringUtils.isBlank(text) ? null : Long.valueOf(text);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(String.valueOf(value));
    }

    private String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value);
        return "null".equals(text) ? null : text;
    }
}
