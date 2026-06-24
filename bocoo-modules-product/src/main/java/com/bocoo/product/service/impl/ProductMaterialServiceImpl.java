package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.bo.ProductMaterialAttributeBo;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMaterialAttribute;
import com.bocoo.product.domain.entity.ProductMaterialType;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMaterialTypeMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductMaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ProductMaterialServiceImpl extends ProductServiceSupport implements ProductMaterialService {

    private final ProductMaterialMapper materialMapper;
    private final ProductMaterialAttributeMapper materialAttributeMapper;
    private final ProductComponentItemMapper componentItemMapper;
    private final ProductMaterialTypeMapper materialTypeMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    @Override
    public TableDataInfo<ProductMaterialVo> queryPageList(ProductMaterialBo bo, PageQuery pageQuery) {
        return page(materialMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByDesc("update_time"));
    }

    @Override
    public List<ProductMaterialVo> queryList(ProductMaterialBo bo) {
        return materialMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByDesc("update_time")));
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
        validateMaterialUnique(bo);
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
        return updateMaterial(bo, true);
    }

    @Override
    public Boolean superUpdateByBo(ProductMaterialBo bo) {
        return updateMaterial(bo, false);
    }

    private Boolean updateMaterial(ProductMaterialBo bo, boolean checkStatus) {
        normalizeMaterial(bo);
        validateMaterialUnique(bo);
        if (checkStatus && bo != null && bo.getMaterialId() != null) {
            ProductMaterial current = materialMapper.selectById(bo.getMaterialId());
            if (current != null) {
                assertNotAudited(current);
                assertNormalEditable(current.getStatus());
            }
        }
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
        for (Long id : ids) {
            assertNoReferences(checkReferences(id));
        }
        return remove(materialMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return materialMapper.update(null, new LambdaUpdateWrapper<ProductMaterial>()
            .eq(ProductMaterial::getMaterialId, id)
            .set(ProductMaterial::getStatus, status)) > 0;
    }

    @Override
    public Boolean audit(Long id) {
        return materialMapper.update(null, new LambdaUpdateWrapper<ProductMaterial>()
            .eq(ProductMaterial::getMaterialId, id)
            .set(ProductMaterial::getAuditStatus, "AUDITED")
            .set(ProductMaterial::getAuditById, currentUserId())
            .set(ProductMaterial::getAuditBy, currentUsername())
            .set(ProductMaterial::getAuditTime, TimeUtils.utcNow())) > 0;
    }

    @Override
    public Boolean unaudit(Long id) {
        return materialMapper.update(null, new LambdaUpdateWrapper<ProductMaterial>()
            .eq(ProductMaterial::getMaterialId, id)
            .set(ProductMaterial::getAuditStatus, "DRAFT")
            .set(ProductMaterial::getAuditById, null)
            .set(ProductMaterial::getAuditBy, null)
            .set(ProductMaterial::getAuditTime, null)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductMaterial material = materialMapper.selectById(id);
        if (material == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        if ("AUDITED".equalsIgnoreCase(material.getAuditStatus())) {
            return deniedEditCheck(material.getStatus(), "product.material.auditedEditDenied", checkReferences(id));
        }
        return editCheckResult(material.getStatus(), checkReferences(id));
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long materialId) {
        long count = materialAttributeMapper.selectCount(activeQuery(ProductMaterialAttribute.class).eq("material_id", materialId))
            + componentItemMapper.selectCount(activeQuery(ProductComponentItem.class).eq("material_id", materialId))
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
            eq(q, "material_type_code", materialTypeCodeOf(bo));
            eq(q, "attribute_group_code", bo.getAttributeGroupCode());
            like(q, "model", bo.getModel());
            like(q, "spec", bo.getSpec());
            like(q, "spec_model_text", bo.getSpecModelText());
            like(q, "manufacturer_code", bo.getManufacturerCode());
            like(q, "manufacturer_name", bo.getManufacturerName());
            like(q, "manufacturer_item_no", bo.getManufacturerItemNo());
            eq(q, "audit_status", bo.getAuditStatus());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalizeMaterial(ProductMaterialBo bo) {
        if (bo == null) {
            return;
        }
        trimMaterialFields(bo);
        validateRequiredFields(bo);
        if (StringUtils.isBlank(bo.getAuditStatus())) {
            bo.setAuditStatus("DRAFT");
        }
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus("ENABLED");
        }
        if (StringUtils.isBlank(bo.getSpecModelText())) {
            bo.setSpecModelText(buildSpecModelText(bo.getModel(), bo.getSpec()));
        }
        normalizeMaterialType(bo);
    }

    private void normalizeMaterialType(ProductMaterialBo bo) {
        String code = materialTypeCodeOf(bo);
        if (StringUtils.isNotBlank(code)) {
            code = code.trim().toUpperCase();
        }
        if (StringUtils.isBlank(code)) {
            throw ServiceException.ofMessageKey("product.material.materialTypeRequired");
        }
        ProductMaterialType type = null;
        if (bo.getMaterialTypeId() != null) {
            type = materialTypeMapper.selectById(bo.getMaterialTypeId());
            if (type != null && StringUtils.isNotBlank(type.getDelFlag()) && !"0".equals(type.getDelFlag())) {
                type = null;
            }
        }
        if (type == null) {
            type = materialTypeMapper.selectOne(activeQuery(ProductMaterialType.class).eq("material_type_code", code));
        }
        if (type == null) {
            throw ServiceException.ofMessageKey("product.material.materialTypeNotFound");
        }
        bo.setMaterialTypeId(type.getMaterialTypeId());
        bo.setMaterialTypeCode(type.getMaterialTypeCode());
        bo.setMaterialTypeNameCn(type.getMaterialTypeNameCn());
        bo.setAttributeGroupId(type.getAttributeGroupId());
        bo.setAttributeGroupCode(type.getAttributeGroupCode());
        bo.setAttributeGroupNameCn(type.getAttributeGroupNameCn());
        bo.setMaterialType(type.getMaterialTypeCode());
    }

    private String materialTypeCodeOf(ProductMaterialBo bo) {
        if (bo == null) {
            return null;
        }
        return StringUtils.blankToDefault(bo.getMaterialTypeCode(), bo.getMaterialType());
    }

    private void validateMaterialUnique(ProductMaterialBo bo) {
        if (bo == null) {
            return;
        }
        validateMaterialCodeUnique(bo);
        validateMaterialNaturalKeyUnique(bo);
    }

    private void validateMaterialCodeUnique(ProductMaterialBo bo) {
        if (StringUtils.isBlank(bo.getMaterialCode())) {
            return;
        }
        QueryWrapper<ProductMaterial> q = activeQuery(ProductMaterial.class)
            .eq("material_code", bo.getMaterialCode())
            .ne(bo.getMaterialId() != null, "material_id", bo.getMaterialId());
        if (materialMapper.selectCount(q) > 0) {
            throw ServiceException.ofMessageKey("product.material.codeExists");
        }
    }

    private void validateMaterialNaturalKeyUnique(ProductMaterialBo bo) {
        if (StringUtils.isBlank(bo.getMaterialNameCn()) || StringUtils.isBlank(bo.getSpec())) {
            return;
        }
        QueryWrapper<ProductMaterial> q = activeQuery(ProductMaterial.class)
            .eq("material_name_cn", bo.getMaterialNameCn())
            .eq("spec", bo.getSpec())
            .ne(bo.getMaterialId() != null, "material_id", bo.getMaterialId());
        if (StringUtils.isNotBlank(bo.getModel())) {
            q.eq("model", bo.getModel());
        } else {
            q.and(wrapper -> wrapper.isNull("model").or().eq("model", ""));
        }
        if (StringUtils.isNotBlank(bo.getManufacturerName())) {
            q.eq("manufacturer_name", bo.getManufacturerName());
        } else {
            q.and(wrapper -> wrapper.isNull("manufacturer_name").or().eq("manufacturer_name", ""));
        }
        if (materialMapper.selectCount(q) > 0) {
            throw ServiceException.ofMessageKey("product.material.naturalKeyExists");
        }
    }

    private void validateRequiredFields(ProductMaterialBo bo) {
        if (StringUtils.isBlank(bo.getMaterialCode())) {
            throw ServiceException.ofMessageKey("product.material.codeRequired");
        }
        if (StringUtils.isBlank(bo.getMaterialNameCn())) {
            throw ServiceException.ofMessageKey("product.material.nameRequired");
        }
        if (StringUtils.isBlank(bo.getSpec())) {
            throw ServiceException.ofMessageKey("product.material.specRequired");
        }
        if (StringUtils.isBlank(materialTypeCodeOf(bo))) {
            throw ServiceException.ofMessageKey("product.material.materialTypeRequired");
        }
        if (StringUtils.isBlank(bo.getUnitCode())) {
            throw ServiceException.ofMessageKey("product.material.unitRequired");
        }
    }

    private void trimMaterialFields(ProductMaterialBo bo) {
        bo.setMaterialCode(trimToNull(bo.getMaterialCode()));
        bo.setMaterialNameCn(trimToNull(bo.getMaterialNameCn()));
        bo.setMaterialNameEn(trimToNull(bo.getMaterialNameEn()));
        bo.setMaterialTypeCode(trimToNull(bo.getMaterialTypeCode()));
        bo.setMaterialType(trimToNull(bo.getMaterialType()));
        bo.setUnitCode(trimToNull(bo.getUnitCode()));
        bo.setSecondaryUnitCode(trimToNull(bo.getSecondaryUnitCode()));
        bo.setManufacturerCode(trimToNull(bo.getManufacturerCode()));
        bo.setManufacturerName(trimToNull(bo.getManufacturerName()));
        bo.setManufacturerItemNo(trimToNull(bo.getManufacturerItemNo()));
        bo.setModel(trimToNull(bo.getModel()));
        bo.setSpec(trimToNull(bo.getSpec()));
        bo.setSpecModelText(trimToNull(bo.getSpecModelText()));
        bo.setColorName(trimToNull(bo.getColorName()));
        bo.setAuditStatus(trimToNull(bo.getAuditStatus()));
        bo.setStatus(trimToNull(bo.getStatus()));
    }

    private String trimToNull(String value) {
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private String buildSpecModelText(String model, String spec) {
        if (StringUtils.isBlank(model)) {
            return spec;
        }
        return "型号：" + model + "；规格：" + spec;
    }

    private void assertNotAudited(ProductMaterial current) {
        if (current != null && "AUDITED".equalsIgnoreCase(current.getAuditStatus())) {
            throw ServiceException.ofMessageKey("product.material.auditedEditDenied");
        }
    }

    private Long currentUserId() {
        try {
            return LoginHelper.getUserId();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String currentUsername() {
        try {
            return LoginHelper.getUsername();
        } catch (Exception ignored) {
            return "system";
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
        Set<String> attributeCodes = new HashSet<>();
        int index = 0;
        for (ProductMaterialAttributeBo item : bo.getAttributeList()) {
            if (item == null || StringUtils.isBlank(item.getAttributeCode())) {
                continue;
            }
            if (!attributeCodes.add(item.getAttributeCode())) {
                throw ServiceException.ofMessageKey("product.materialAttribute.codeExists");
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
