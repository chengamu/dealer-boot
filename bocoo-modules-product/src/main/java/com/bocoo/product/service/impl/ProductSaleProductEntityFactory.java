package com.bocoo.product.service.impl;

import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.entity.ProductSaleProduct;

final class ProductSaleProductEntityFactory {

    private ProductSaleProductEntityFactory() {
    }

    static ProductSaleProduct toEntity(ProductSaleProductBo bo) {
        ProductSaleProduct entity = new ProductSaleProduct();
        entity.setSaleProductId(bo.getSaleProductId());
        entity.setTenantId(bo.getTenantId());
        entity.setSaleProductCode(bo.getSaleProductCode());
        entity.setSaleProductName(bo.getSaleProductName());
        entity.setCategoryId(bo.getCategoryId());
        entity.setCategoryCode(bo.getCategoryCode());
        entity.setCategoryNameCn(bo.getCategoryNameCn());
        entity.setProductTypeCode(bo.getProductTypeCode());
        entity.setProductTypeNameCn(bo.getProductTypeNameCn());
        entity.setFormulaId(bo.getFormulaId());
        entity.setFormulaCode(bo.getFormulaCode());
        entity.setFormulaName(bo.getFormulaName());
        entity.setFormulaVersionId(bo.getFormulaVersionId());
        entity.setFormulaVersionNo(bo.getFormulaVersionNo());
        entity.setFormulaVersionLabel(bo.getFormulaVersionLabel());
        entity.setMinWidthInch(bo.getMinWidthInch());
        entity.setMinHeightInch(bo.getMinHeightInch());
        entity.setMaxWidthInch(bo.getMaxWidthInch());
        entity.setMaxHeightInch(bo.getMaxHeightInch());
        entity.setSizeSummary(bo.getSizeSummary());
        entity.setPriceStatus(bo.getPriceStatus());
        entity.setStatus(bo.getStatus());
        entity.setSortOrder(bo.getSortOrder());
        entity.setRemark(bo.getRemark());
        return entity;
    }

    static ProductSaleProduct copyStatus(ProductSaleProduct source) {
        ProductSaleProduct copy = new ProductSaleProduct();
        copy.setSaleProductId(source.getSaleProductId());
        copy.setSaleProductCode(source.getSaleProductCode());
        copy.setSaleProductName(source.getSaleProductName());
        copy.setPriceStatus(source.getPriceStatus());
        copy.setStatus(source.getStatus());
        return copy;
    }
}
