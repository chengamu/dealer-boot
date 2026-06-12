package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductBaseAttributeBo;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.bo.ProductUnitBo;
import com.bocoo.product.domain.entity.*;
import com.bocoo.product.domain.vo.*;
import com.bocoo.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分类、单位和配置字典基础资料服务。
 */
@Service
@RequiredArgsConstructor
public class ProductBaseInfoService extends ProductCrudSupport {

    private final ProductCategoryMapper categoryMapper;
    private final ProductUnitMapper unitMapper;
    private final ProductBaseAttributeMapper baseAttributeMapper;
    private final ProductMaterialMapper materialMapper;
    private final ProductComponentMapper componentMapper;
    private final ProductComponentItemMapper componentItemMapper;
    private final ProductMaterialAttributeMapper materialAttributeMapper;
    private final FabricProfileMapper fabricProfileMapper;

    public TableDataInfo<ProductCategoryVo> queryProductCategoryPage(ProductCategoryBo bo, PageQuery pageQuery) {
        return page(categoryMapper, pageQuery, categoryWrapper(bo));
    }

    public List<ProductCategoryVo> queryProductCategoryList(ProductCategoryBo bo) {
        return categoryMapper.selectVoList(categoryWrapper(bo));
    }

    public ProductCategoryVo getProductCategoryById(Long id) {
        return categoryMapper.selectVoById(id);
    }

    public Boolean saveProductCategory(ProductCategoryBo bo) {
        return save(categoryMapper, bo, ProductCategory.class, "categoryId");
    }

    public Boolean removeProductCategoryByIds(Long[] ids) {
        return remove(categoryMapper, ids);
    }

    public Boolean updateProductCategoryStatus(Long id, String status) {
        return updateStatus(categoryMapper, ProductCategory.class, "categoryId", id, status);
    }

    public ReferenceCheckResultVo checkProductCategoryReferences(Long categoryId) {
        return referenceResult(0, null, null);
    }

    public TableDataInfo<ProductUnitVo> queryProductUnitPage(ProductUnitBo bo, PageQuery pageQuery) {
        return page(unitMapper, pageQuery, unitWrapper(bo));
    }

    public List<ProductUnitVo> queryProductUnitList(ProductUnitBo bo) {
        return unitMapper.selectVoList(unitWrapper(bo));
    }

    public ProductUnitVo getProductUnitById(Long id) {
        return unitMapper.selectVoById(id);
    }

    public Boolean saveProductUnit(ProductUnitBo bo) {
        return save(unitMapper, bo, ProductUnit.class, "unitId");
    }

    public Boolean removeProductUnitByIds(Long[] ids) {
        return remove(unitMapper, ids);
    }

    public Boolean updateProductUnitStatus(Long id, String status) {
        return updateStatus(unitMapper, ProductUnit.class, "unitId", id, status);
    }

    public ReferenceCheckResultVo checkProductUnitReferences(Long unitId) {
        ProductUnit unit = unitMapper.selectById(unitId);
        if (unit == null || unit.getUnitCode() == null) {
            return referenceResult(0, null, null);
        }
        String code = unit.getUnitCode();
        long count = materialMapper.selectCount(activeQuery(ProductMaterial.class).eq("unit_code", code))
            + componentMapper.selectCount(activeQuery(ProductComponent.class).eq("unit_code", code))
            + componentItemMapper.selectCount(activeQuery(ProductComponentItem.class).eq("unit_code", code))
            + fabricProfileMapper.selectCount(activeQuery(FabricProfile.class)
                .and(q -> q.eq("width_unit", code).or().eq("thickness_unit", code)
                    .or().eq("purchase_unit_code", code).or().eq("inventory_unit_code", code).or().eq("sales_unit_code", code)));
        return referenceResult(count, "product.unit.hasReferences", "Unit code references: " + count);
    }

    public TableDataInfo<ProductBaseAttributeVo> queryProductBaseAttributePage(ProductBaseAttributeBo bo, PageQuery pageQuery) {
        return page(baseAttributeMapper, pageQuery, baseAttributeWrapper(bo));
    }

    public List<ProductBaseAttributeVo> queryProductBaseAttributeList(ProductBaseAttributeBo bo) {
        return baseAttributeMapper.selectVoList(baseAttributeWrapper(bo));
    }

    public ProductBaseAttributeVo getProductBaseAttributeById(Long id) {
        return baseAttributeMapper.selectVoById(id);
    }

    public Boolean saveProductBaseAttribute(ProductBaseAttributeBo bo) {
        return save(baseAttributeMapper, bo, ProductBaseAttribute.class, "attributeId");
    }

    public Boolean removeProductBaseAttributeByIds(Long[] ids) {
        return remove(baseAttributeMapper, ids);
    }

    public Boolean updateProductBaseAttributeStatus(Long id, String status) {
        return updateStatus(baseAttributeMapper, ProductBaseAttribute.class, "attributeId", id, status);
    }

    public ReferenceCheckResultVo checkProductBaseAttributeReferences(Long attributeId) {
        long count = materialAttributeMapper.selectCount(activeQuery(ProductMaterialAttribute.class).eq("attribute_id", attributeId));
        return referenceResult(count, "product.baseAttribute.hasReferences", "Material attributes: " + count);
    }

    private QueryWrapper<ProductCategory> categoryWrapper(ProductCategoryBo bo) {
        QueryWrapper<ProductCategory> q = activeQuery(ProductCategory.class);
        if (bo != null) {
            like(q, "category_code", bo.getCategoryCode());
            if (StringUtils.isNotBlank(bo.getCategoryNameCn())) {
                q.and(wrapper -> wrapper.like("category_name_cn", bo.getCategoryNameCn()).or().like("category_name_en", bo.getCategoryNameCn()));
            }
            like(q, "category_name_en", bo.getCategoryNameEn());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "category_id");
    }

    private QueryWrapper<ProductUnit> unitWrapper(ProductUnitBo bo) {
        QueryWrapper<ProductUnit> q = activeQuery(ProductUnit.class);
        if (bo != null) {
            like(q, "unit_code", bo.getUnitCode());
            if (StringUtils.isNotBlank(bo.getUnitNameCn())) {
                q.and(wrapper -> wrapper.like("unit_name_cn", bo.getUnitNameCn()).or().like("unit_name_en", bo.getUnitNameCn()));
            }
            eq(q, "unit_type", bo.getUnitType());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "unit_id");
    }

    private QueryWrapper<ProductBaseAttribute> baseAttributeWrapper(ProductBaseAttributeBo bo) {
        QueryWrapper<ProductBaseAttribute> q = activeQuery(ProductBaseAttribute.class);
        if (bo != null) {
            eq(q, "attribute_group", bo.getAttributeGroup());
            like(q, "attribute_code", bo.getAttributeCode());
            if (StringUtils.isNotBlank(bo.getAttributeNameCn())) {
                q.and(wrapper -> wrapper.like("attribute_name_cn", bo.getAttributeNameCn()).or().like("attribute_name_en", bo.getAttributeNameCn()));
            }
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "attribute_id");
    }
}
