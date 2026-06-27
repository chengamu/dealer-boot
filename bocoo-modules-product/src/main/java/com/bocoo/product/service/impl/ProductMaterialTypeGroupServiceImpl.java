package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialTypeGroupBo;
import com.bocoo.product.domain.entity.ProductBaseAttribute;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMaterialType;
import com.bocoo.product.domain.entity.ProductMaterialTypeGroup;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialTypeGroupVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductBaseAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductMaterialTypeGroupMapper;
import com.bocoo.product.mapper.ProductMaterialTypeMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductMaterialTypeGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductMaterialTypeGroupServiceImpl extends ProductServiceSupport implements ProductMaterialTypeGroupService {

    private final ProductMaterialTypeGroupMapper materialTypeGroupMapper;
    private final ProductMaterialTypeMapper materialTypeMapper;
    private final ProductBaseAttributeMapper baseAttributeMapper;
    private final ProductMaterialMapper materialMapper;

    @Override
    public TableDataInfo<ProductMaterialTypeGroupVo> queryPageList(ProductMaterialTypeGroupBo bo, PageQuery pageQuery) {
        return page(materialTypeGroupMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "group_id"));
    }

    @Override
    public List<ProductMaterialTypeGroupVo> queryList(ProductMaterialTypeGroupBo bo) {
        return materialTypeGroupMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "group_id")));
    }

    @Override
    public ProductMaterialTypeGroupVo queryById(Long id) {
        return materialTypeGroupMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductMaterialTypeGroupBo bo) {
        normalizeGroup(bo);
        validateGroupCodeUnique(bo);
        ProductMaterialTypeGroup entity = MapstructUtils.convert(bo, ProductMaterialTypeGroup.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        entity.setFormulaSummaryVisibleFlag(bo.getFormulaSummaryVisibleFlag());
        ProductEntityDefaults.prepareInsert(entity);
        return materialTypeGroupMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductMaterialTypeGroupBo bo) {
        normalizeGroup(bo);
        ProductMaterialTypeGroup current = null;
        if (bo != null && bo.getGroupId() != null) {
            current = materialTypeGroupMapper.selectById(bo.getGroupId());
            if (current != null) {
                assertEntityEditable(current.getSystemFlag(), current.getEditableFlag());
                if (onlyFormulaSummaryVisibleChanged(current, bo)) {
                    return materialTypeGroupMapper.update(null, new LambdaUpdateWrapper<ProductMaterialTypeGroup>()
                        .eq(ProductMaterialTypeGroup::getGroupId, bo.getGroupId())
                        .set(ProductMaterialTypeGroup::getFormulaSummaryVisibleFlag, bo.getFormulaSummaryVisibleFlag())) > 0;
                }
                assertNormalEditable(current.getStatus());
            }
        }
        validateGroupCodeUnique(bo);
        ProductMaterialTypeGroup entity = MapstructUtils.convert(bo, ProductMaterialTypeGroup.class);
        if (entity != null) {
            entity.setFormulaSummaryVisibleFlag(bo.getFormulaSummaryVisibleFlag());
        }
        return entity != null && materialTypeGroupMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            ProductMaterialTypeGroup current = materialTypeGroupMapper.selectById(id);
            if (current != null && Boolean.TRUE.equals(current.getSystemFlag())) {
                throw ServiceException.ofMessageKey("product.materialTypeGroup.systemCannotDelete");
            }
            if (current != null) {
                assertDisabledBeforeDelete(current.getStatus());
            }
            assertNoReferences(checkReferences(id));
        }
        return remove(materialTypeGroupMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        ProductMaterialTypeGroup current = materialTypeGroupMapper.selectById(id);
        if (current != null) {
            assertEntityEditable(current.getSystemFlag(), current.getEditableFlag());
        }
        return materialTypeGroupMapper.update(null, new LambdaUpdateWrapper<ProductMaterialTypeGroup>()
            .eq(ProductMaterialTypeGroup::getGroupId, id)
            .set(ProductMaterialTypeGroup::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductMaterialTypeGroup current = materialTypeGroupMapper.selectById(id);
        if (current == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        ReferenceCheckResultVo references = checkReferences(id);
        if (isEntityLocked(current.getSystemFlag(), current.getEditableFlag())) {
            return deniedEditCheck(current.getStatus(), "product.materialTypeGroup.notEditable", references);
        }
        return editCheckResult(current.getStatus(), references);
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long groupId) {
        ProductMaterialTypeGroup current = materialTypeGroupMapper.selectById(groupId);
        if (current == null) {
            return referenceResult(0, null, null);
        }
        long typeCount = materialTypeMapper.selectCount(activeQuery(ProductMaterialType.class).eq("attribute_group_id", groupId));
        long attributeCount = baseAttributeMapper.selectCount(activeQuery(ProductBaseAttribute.class).eq("attribute_group_code", current.getGroupCode()));
        long materialCount = materialMapper.selectCount(activeQuery(ProductMaterial.class).eq("attribute_group_id", groupId));
        ReferenceCheckResultVo result = referenceResult(typeCount + attributeCount + materialCount, "product.materialTypeGroup.hasReferences", null);
        if (typeCount > 0) {
            result.getReferenceSummaries().add("Material types: " + typeCount);
        }
        if (attributeCount > 0) {
            result.getReferenceSummaries().add("Material attributes: " + attributeCount);
        }
        if (materialCount > 0) {
            result.getReferenceSummaries().add("Materials: " + materialCount);
        }
        return result;
    }

    private QueryWrapper<ProductMaterialTypeGroup> buildQueryWrapper(ProductMaterialTypeGroupBo bo) {
        QueryWrapper<ProductMaterialTypeGroup> q = activeQuery(ProductMaterialTypeGroup.class);
        if (bo != null) {
            like(q, "group_code", bo.getGroupCode());
            if (StringUtils.isNotBlank(bo.getGroupNameCn())) {
                q.and(wrapper -> wrapper.like("group_name_cn", bo.getGroupNameCn()).or().like("group_name_en", bo.getGroupNameCn()));
            }
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalizeGroup(ProductMaterialTypeGroupBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getGroupCode())) {
            throw ServiceException.ofMessageKey("product.materialTypeGroup.codeRequired");
        }
        bo.setGroupCode(bo.getGroupCode().trim().toUpperCase());
        if (bo.getSystemFlag() == null) {
            bo.setSystemFlag(Boolean.FALSE);
        }
        if (bo.getEditableFlag() == null) {
            bo.setEditableFlag(Boolean.TRUE);
        }
        if (bo.getFormulaSummaryVisibleFlag() == null) {
            bo.setFormulaSummaryVisibleFlag(Boolean.TRUE);
        }
    }

    private void validateGroupCodeUnique(ProductMaterialTypeGroupBo bo) {
        long count = materialTypeGroupMapper.selectCount(activeQuery(ProductMaterialTypeGroup.class)
            .eq("group_code", bo.getGroupCode())
            .ne(bo.getGroupId() != null, "group_id", bo.getGroupId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.materialTypeGroup.codeExists");
        }
    }

    private boolean onlyFormulaSummaryVisibleChanged(ProductMaterialTypeGroup current, ProductMaterialTypeGroupBo bo) {
        return Objects.equals(current.getGroupCode(), bo.getGroupCode())
            && Objects.equals(current.getGroupNameCn(), bo.getGroupNameCn())
            && Objects.equals(current.getGroupNameEn(), bo.getGroupNameEn())
            && Objects.equals(current.getSystemFlag(), bo.getSystemFlag())
            && Objects.equals(current.getEditableFlag(), bo.getEditableFlag())
            && Objects.equals(current.getStatus(), bo.getStatus())
            && Objects.equals(current.getSortOrder(), bo.getSortOrder())
            && Objects.equals(current.getRemark(), bo.getRemark())
            && !Objects.equals(current.getFormulaSummaryVisibleFlag(), bo.getFormulaSummaryVisibleFlag());
    }

    private void assertEntityEditable(Boolean systemFlag, Boolean editableFlag) {
        if (isEntityLocked(systemFlag, editableFlag)) {
            throw ServiceException.ofMessageKey("product.materialTypeGroup.notEditable");
        }
    }

    private boolean isEntityLocked(Boolean systemFlag, Boolean editableFlag) {
        return Boolean.TRUE.equals(systemFlag) && Boolean.FALSE.equals(editableFlag);
    }
}
