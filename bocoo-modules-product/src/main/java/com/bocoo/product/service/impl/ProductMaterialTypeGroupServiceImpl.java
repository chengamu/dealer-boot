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
        ProductEntityDefaults.prepareInsert(entity);
        return materialTypeGroupMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductMaterialTypeGroupBo bo) {
        normalizeGroup(bo);
        validateGroupCodeUnique(bo);
        if (bo != null && bo.getGroupId() != null) {
            ProductMaterialTypeGroup current = materialTypeGroupMapper.selectById(bo.getGroupId());
            if (current != null) {
                assertEntityEditable(current.getSystemFlag(), current.getEditableFlag());
                assertNormalEditable(current.getStatus());
            }
        }
        ProductMaterialTypeGroup entity = MapstructUtils.convert(bo, ProductMaterialTypeGroup.class);
        return entity != null && materialTypeGroupMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            ProductMaterialTypeGroup current = materialTypeGroupMapper.selectById(id);
            if (current != null && Boolean.TRUE.equals(current.getSystemFlag())) {
                throw ServiceException.ofMessageKey("product.materialTypeGroup.systemCannotDelete");
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
    }

    private void validateGroupCodeUnique(ProductMaterialTypeGroupBo bo) {
        long count = materialTypeGroupMapper.selectCount(activeQuery(ProductMaterialTypeGroup.class)
            .eq("group_code", bo.getGroupCode())
            .ne(bo.getGroupId() != null, "group_id", bo.getGroupId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.materialTypeGroup.codeExists");
        }
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
