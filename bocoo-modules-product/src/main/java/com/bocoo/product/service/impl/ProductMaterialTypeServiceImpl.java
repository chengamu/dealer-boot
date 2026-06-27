package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialTypeBo;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMaterialType;
import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialTypeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMaterialTypeGroupMapper;
import com.bocoo.product.mapper.ProductMaterialTypeMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductMaterialTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMaterialTypeServiceImpl extends ProductServiceSupport implements ProductMaterialTypeService {

    private final ProductMaterialTypeMapper materialTypeMapper;
    private final ProductMaterialTypeGroupMapper materialTypeGroupMapper;
    private final ProductMaterialMapper materialMapper;

    @Override
    public TableDataInfo<ProductMaterialTypeVo> queryPageList(ProductMaterialTypeBo bo, PageQuery pageQuery) {
        return page(materialTypeMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("attribute_group_code", "sort_order", "material_type_id"));
    }

    @Override
    public List<ProductMaterialTypeVo> queryList(ProductMaterialTypeBo bo) {
        return materialTypeMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("attribute_group_code", "sort_order", "material_type_id")));
    }

    @Override
    public ProductMaterialTypeVo queryById(Long id) {
        return materialTypeMapper.selectVoById(id);
    }

    @Override
    public ProductMaterialTypeVo queryByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        return materialTypeMapper.selectVoOne(activeQuery(ProductMaterialType.class).eq("material_type_code", code));
    }

    @Override
    public Boolean insertByBo(ProductMaterialTypeBo bo) {
        normalizeType(bo);
        validateMaterialTypeCodeUnique(bo);
        ProductMaterialType entity = MapstructUtils.convert(bo, ProductMaterialType.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return materialTypeMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductMaterialTypeBo bo) {
        normalizeType(bo);
        validateMaterialTypeCodeUnique(bo);
        if (bo != null && bo.getMaterialTypeId() != null) {
            ProductMaterialType current = materialTypeMapper.selectById(bo.getMaterialTypeId());
            if (current != null) {
                assertEntityEditable(current.getSystemFlag(), current.getEditableFlag());
                assertNormalEditable(current.getStatus());
            }
        }
        ProductMaterialType entity = MapstructUtils.convert(bo, ProductMaterialType.class);
        return entity != null && materialTypeMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            ProductMaterialType current = materialTypeMapper.selectById(id);
            if (current != null && Boolean.TRUE.equals(current.getSystemFlag())) {
                throw ServiceException.ofMessageKey("product.materialType.systemCannotDelete");
            }
            if (current != null) {
                assertDisabledBeforeDelete(current.getStatus());
            }
            assertNoReferences(checkReferences(id));
        }
        return remove(materialTypeMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        ProductMaterialType current = materialTypeMapper.selectById(id);
        if (current != null) {
            assertEntityEditable(current.getSystemFlag(), current.getEditableFlag());
        }
        return materialTypeMapper.update(null, new LambdaUpdateWrapper<ProductMaterialType>()
            .eq(ProductMaterialType::getMaterialTypeId, id)
            .set(ProductMaterialType::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductMaterialType current = materialTypeMapper.selectById(id);
        if (current == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        ReferenceCheckResultVo references = checkReferences(id);
        if (isEntityLocked(current.getSystemFlag(), current.getEditableFlag())) {
            return deniedEditCheck(current.getStatus(), "product.materialType.notEditable", references);
        }
        return editCheckResult(current.getStatus(), references);
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long materialTypeId) {
        ProductMaterialType current = materialTypeMapper.selectById(materialTypeId);
        if (current == null) {
            return referenceResult(0, null, null);
        }
        long materialCount = materialMapper.selectCount(activeQuery(ProductMaterial.class).eq("material_type_id", materialTypeId));
        ReferenceCheckResultVo result = referenceResult(materialCount, "product.materialType.hasReferences", null);
        if (materialCount > 0) {
            result.getReferenceSummaries().add("Materials: " + materialCount);
        }
        return result;
    }

    private QueryWrapper<ProductMaterialType> buildQueryWrapper(ProductMaterialTypeBo bo) {
        QueryWrapper<ProductMaterialType> q = activeQuery(ProductMaterialType.class);
        if (bo != null) {
            like(q, "material_type_code", bo.getMaterialTypeCode());
            if (StringUtils.isNotBlank(bo.getMaterialTypeNameCn())) {
                q.and(wrapper -> wrapper.like("material_type_name_cn", bo.getMaterialTypeNameCn()).or().like("material_type_name_en", bo.getMaterialTypeNameCn()));
            }
            eq(q, "attribute_group_code", bo.getAttributeGroupCode());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalizeType(ProductMaterialTypeBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getMaterialTypeCode())) {
            throw ServiceException.ofMessageKey("product.materialType.codeRequired");
        }
        bo.setMaterialTypeCode(bo.getMaterialTypeCode().trim().toUpperCase());
        ProductMaterialTypeGroup group = findGroup(bo);
        if (group == null) {
            throw ServiceException.ofMessageKey("product.materialType.groupRequired");
        }
        bo.setAttributeGroupId(group.getGroupId());
        bo.setAttributeGroupCode(group.getGroupCode());
        bo.setAttributeGroupNameCn(group.getGroupNameCn());
        if (bo.getSystemFlag() == null) {
            bo.setSystemFlag(Boolean.FALSE);
        }
        if (bo.getEditableFlag() == null) {
            bo.setEditableFlag(Boolean.TRUE);
        }
    }

    private ProductMaterialTypeGroup findGroup(ProductMaterialTypeBo bo) {
        if (bo.getAttributeGroupId() != null) {
            ProductMaterialTypeGroup group = materialTypeGroupMapper.selectById(bo.getAttributeGroupId());
            if (group != null && (StringUtils.isBlank(group.getDelFlag()) || "0".equals(group.getDelFlag()))) {
                return group;
            }
        }
        if (StringUtils.isNotBlank(bo.getAttributeGroupCode())) {
            return materialTypeGroupMapper.selectOne(activeQuery(ProductMaterialTypeGroup.class).eq("group_code", bo.getAttributeGroupCode()));
        }
        return null;
    }

    private void validateMaterialTypeCodeUnique(ProductMaterialTypeBo bo) {
        long count = materialTypeMapper.selectCount(activeQuery(ProductMaterialType.class)
            .eq("material_type_code", bo.getMaterialTypeCode())
            .ne(bo.getMaterialTypeId() != null, "material_type_id", bo.getMaterialTypeId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.materialType.codeExists");
        }
    }

    private void assertEntityEditable(Boolean systemFlag, Boolean editableFlag) {
        if (isEntityLocked(systemFlag, editableFlag)) {
            throw ServiceException.ofMessageKey("product.materialType.notEditable");
        }
    }

    private boolean isEntityLocked(Boolean systemFlag, Boolean editableFlag) {
        return Boolean.TRUE.equals(systemFlag) && Boolean.FALSE.equals(editableFlag);
    }

}
