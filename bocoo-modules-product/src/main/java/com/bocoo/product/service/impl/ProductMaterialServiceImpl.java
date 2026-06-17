package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.bo.ProductMaterialAttributeBo;
import com.bocoo.product.domain.entity.FabricProfile;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMaterialAttribute;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.FabricProfileMapper;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMaterialServiceImpl extends ProductServiceSupport implements ProductMaterialService {

    private final ProductMaterialMapper materialMapper;
    private final ProductMaterialAttributeMapper materialAttributeMapper;
    private final ProductComponentItemMapper componentItemMapper;
    private final FabricProfileMapper fabricProfileMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    @Override
    public TableDataInfo<ProductMaterialVo> queryPageList(ProductMaterialBo bo, PageQuery pageQuery) {
        return page(materialMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ProductMaterialVo> queryList(ProductMaterialBo bo) {
        return materialMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductMaterialVo queryById(Long id) {
        ProductMaterialVo vo = materialMapper.selectVoById(id);
        if (vo != null) {
            vo.setAttributeList(queryAttributes(id));
        }
        return vo;
    }

    @Override
    public Boolean insertByBo(ProductMaterialBo bo) {
        normalizeMaterial(bo);
        ProductMaterial entity = MapstructUtils.convert(bo, ProductMaterial.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        boolean inserted = materialMapper.insert(entity) > 0;
        if (inserted) {
            syncAttributes(entity.getMaterialId(), bo);
        }
        return inserted;
    }

    @Override
    public Boolean updateByBo(ProductMaterialBo bo) {
        normalizeMaterial(bo);
        ProductMaterial entity = MapstructUtils.convert(bo, ProductMaterial.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        boolean updated = materialMapper.updateById(entity) > 0;
        if (updated) {
            syncAttributes(entity.getMaterialId(), bo);
        }
        return updated;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(materialMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return materialMapper.update(null, new LambdaUpdateWrapper<ProductMaterial>()
            .eq(ProductMaterial::getMaterialId, id)
            .set(ProductMaterial::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long materialId) {
        long count = materialAttributeMapper.selectCount(activeQuery(ProductMaterialAttribute.class).eq("material_id", materialId))
            + componentItemMapper.selectCount(activeQuery(ProductComponentItem.class).eq("material_id", materialId))
            + fabricProfileMapper.selectCount(activeQuery(FabricProfile.class).eq("material_id", materialId))
            + mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "MATERIAL").eq("target_id", materialId));
        return referenceResult(count, "product.material.hasReferences", "Material references: " + count);
    }

    private QueryWrapper<ProductMaterial> buildQueryWrapper(ProductMaterialBo bo) {
        QueryWrapper<ProductMaterial> q = activeQuery(ProductMaterial.class);
        if (bo != null) {
            like(q, "material_code", bo.getMaterialCode());
            if (StringUtils.isNotBlank(bo.getMaterialNameCn())) {
                q.and(wrapper -> wrapper.like("material_name_cn", bo.getMaterialNameCn()).or().like("material_name_en", bo.getMaterialNameCn()));
            }
            like(q, "material_name_en", bo.getMaterialNameEn());
            eq(q, "material_type", bo.getMaterialType());
            eq(q, "business_type", bo.getBusinessType());
            like(q, "spec_summary", bo.getSpecSummary());
            like(q, "supplier_code", bo.getSupplierCode());
            like(q, "supplier_name", bo.getSupplierName());
            like(q, "vendor_item_no", bo.getVendorItemNo());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByDesc("update_time");
    }

    private void normalizeMaterial(ProductMaterialBo bo) {
        if (bo == null) {
            return;
        }
        if (StringUtils.isBlank(bo.getSpecSummary())) {
            bo.setSpecSummary(StringUtils.blankToDefault(bo.getAttributeSummary(), bo.getPrimarySpec()));
        }
        if (StringUtils.isBlank(bo.getPurchaseUnitCode())) {
            bo.setPurchaseUnitCode(bo.getUnitCode());
        }
        if (StringUtils.isBlank(bo.getInventoryUnitCode())) {
            bo.setInventoryUnitCode(bo.getUnitCode());
        }
        if (StringUtils.isBlank(bo.getUsageUnitCode())) {
            bo.setUsageUnitCode(bo.getUnitCode());
        }
        if (bo.getPurchaseEnabled() == null) {
            bo.setPurchaseEnabled(Boolean.FALSE);
        }
        if (bo.getInventoryEnabled() == null) {
            bo.setInventoryEnabled(Boolean.FALSE);
        }
        if (StringUtils.isBlank(bo.getPriceCurrencyCode())) {
            bo.setPriceCurrencyCode("CNY");
        }
    }

    private List<ProductMaterialAttributeVo> queryAttributes(Long materialId) {
        return materialAttributeMapper.selectVoList(activeQuery(ProductMaterialAttribute.class)
            .eq("material_id", materialId)
            .orderByAsc("sort_order", "material_attribute_id"));
    }

    private void syncAttributes(Long materialId, ProductMaterialBo bo) {
        if (materialId == null || bo.getAttributeList() == null) {
            return;
        }
        materialAttributeMapper.delete(activeQuery(ProductMaterialAttribute.class).eq("material_id", materialId));
        int index = 0;
        for (ProductMaterialAttributeBo item : bo.getAttributeList()) {
            if (item == null || StringUtils.isBlank(item.getAttributeCode())) {
                continue;
            }
            ProductMaterialAttribute entity = MapstructUtils.convert(item, ProductMaterialAttribute.class);
            if (entity == null) {
                continue;
            }
            entity.setAttributeValueId(null);
            entity.setMaterialId(materialId);
            entity.setMaterialCode(bo.getMaterialCode());
            if (entity.getSortOrder() == null) {
                entity.setSortOrder(index * 10 + 10);
            }
            if (StringUtils.isBlank(entity.getStatus())) {
                entity.setStatus("ENABLED");
            }
            ProductEntityDefaults.prepareInsert(entity);
            materialAttributeMapper.insert(entity);
            index++;
        }
    }
}
