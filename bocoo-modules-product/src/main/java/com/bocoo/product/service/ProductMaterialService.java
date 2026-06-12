package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialAttributeBo;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.entity.*;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 物料和通用物料属性服务。
 */
@Service
@RequiredArgsConstructor
public class ProductMaterialService extends ProductCrudSupport {

    private final ProductMaterialMapper materialMapper;
    private final ProductMaterialAttributeMapper materialAttributeMapper;
    private final ProductComponentItemMapper componentItemMapper;
    private final FabricProfileMapper fabricProfileMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    public TableDataInfo<ProductMaterialVo> queryProductMaterialPage(ProductMaterialBo bo, PageQuery pageQuery) {
        return page(materialMapper, pageQuery, materialWrapper(bo));
    }

    public List<ProductMaterialVo> queryProductMaterialList(ProductMaterialBo bo) {
        return materialMapper.selectVoList(materialWrapper(bo));
    }

    public ProductMaterialVo getProductMaterialById(Long id) {
        return materialMapper.selectVoById(id);
    }

    public Boolean saveProductMaterial(ProductMaterialBo bo) {
        return save(materialMapper, bo, ProductMaterial.class, "materialId");
    }

    public Boolean removeProductMaterialByIds(Long[] ids) {
        return remove(materialMapper, ids);
    }

    public Boolean updateProductMaterialStatus(Long id, String status) {
        return updateStatus(materialMapper, ProductMaterial.class, "materialId", id, status);
    }

    public ReferenceCheckResultVo checkProductMaterialReferences(Long materialId) {
        long count = materialAttributeMapper.selectCount(activeQuery(ProductMaterialAttribute.class).eq("material_id", materialId))
            + componentItemMapper.selectCount(activeQuery(ProductComponentItem.class).eq("material_id", materialId))
            + fabricProfileMapper.selectCount(activeQuery(FabricProfile.class).eq("material_id", materialId))
            + mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "MATERIAL").eq("target_id", materialId));
        return referenceResult(count, "product.material.hasReferences", "Material references: " + count);
    }

    public TableDataInfo<ProductMaterialAttributeVo> queryProductMaterialAttributePage(ProductMaterialAttributeBo bo, PageQuery pageQuery) {
        return page(materialAttributeMapper, pageQuery, materialAttributeWrapper(bo));
    }

    public List<ProductMaterialAttributeVo> queryProductMaterialAttributeList(ProductMaterialAttributeBo bo) {
        return materialAttributeMapper.selectVoList(materialAttributeWrapper(bo));
    }

    public ProductMaterialAttributeVo getProductMaterialAttributeById(Long id) {
        return materialAttributeMapper.selectVoById(id);
    }

    public Boolean saveProductMaterialAttribute(ProductMaterialAttributeBo bo) {
        return save(materialAttributeMapper, bo, ProductMaterialAttribute.class, "attributeValueId");
    }

    public Boolean removeProductMaterialAttributeByIds(Long[] ids) {
        return remove(materialAttributeMapper, ids);
    }

    public Boolean updateProductMaterialAttributeStatus(Long id, String status) {
        return updateStatus(materialAttributeMapper, ProductMaterialAttribute.class, "attributeValueId", id, status);
    }

    public ReferenceCheckResultVo checkProductMaterialAttributeReferences(Long id) {
        return referenceResult(0, null, null);
    }

    private QueryWrapper<ProductMaterial> materialWrapper(ProductMaterialBo bo) {
        QueryWrapper<ProductMaterial> q = activeQuery(ProductMaterial.class);
        if (bo != null) {
            like(q, "material_code", bo.getMaterialCode());
            if (StringUtils.isNotBlank(bo.getMaterialNameCn())) {
                q.and(wrapper -> wrapper.like("material_name_cn", bo.getMaterialNameCn()).or().like("material_name_en", bo.getMaterialNameCn()));
            }
            like(q, "material_name_en", bo.getMaterialNameEn());
            eq(q, "material_type", bo.getMaterialType());
            eq(q, "business_type", bo.getBusinessType());
            like(q, "supplier_name", bo.getSupplierName());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByDesc("update_time");
    }

    private QueryWrapper<ProductMaterialAttribute> materialAttributeWrapper(ProductMaterialAttributeBo bo) {
        QueryWrapper<ProductMaterialAttribute> q = activeQuery(ProductMaterialAttribute.class);
        if (bo != null) {
            eq(q, "material_id", bo.getMaterialId());
            like(q, "material_code", bo.getMaterialCode());
            like(q, "attribute_code", bo.getAttributeCode());
            like(q, "attribute_name_cn", bo.getAttributeNameCn());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "material_attribute_id");
    }
}
