package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductDictTypeBo;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.product.domain.entity.ProductDictType;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductDictTypeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductDictItemMapper;
import com.bocoo.product.mapper.ProductDictTypeMapper;
import com.bocoo.product.service.ProductDictTypeService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductDictTypeServiceImpl extends ProductServiceSupport implements ProductDictTypeService {

    private final ProductDictTypeMapper dictTypeMapper;
    private final ProductDictItemMapper dictItemMapper;

    @Override
    public TableDataInfo<ProductDictTypeVo> queryPageList(ProductDictTypeBo bo, PageQuery pageQuery) {
        return page(dictTypeMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "dict_type_id"));
    }

    @Override
    public List<ProductDictTypeVo> queryList(ProductDictTypeBo bo) {
        return dictTypeMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "dict_type_id")));
    }

    @Override
    public ProductDictTypeVo queryById(Long id) {
        return dictTypeMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductDictTypeBo bo) {
        validateDictType(bo);
        ProductDictType entity = MapstructUtils.convert(bo, ProductDictType.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return dictTypeMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductDictTypeBo bo) {
        validateDictType(bo);
        if (bo != null && bo.getDictTypeId() != null) {
            ProductDictType current = dictTypeMapper.selectById(bo.getDictTypeId());
            if (current != null) {
                assertDictEditable(current.getSystemFlag(), current.getEditableFlag());
                assertNormalEditable(current.getStatus());
            }
        }
        ProductDictType entity = MapstructUtils.convert(bo, ProductDictType.class);
        return entity != null && dictTypeMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            ProductDictType entity = dictTypeMapper.selectById(id);
            if (entity != null && Boolean.TRUE.equals(entity.getSystemFlag())) {
                throw ServiceException.ofMessageKey("product.dict.systemTypeCannotDelete");
            }
            if (entity != null) {
                assertDisabledBeforeDelete(entity.getStatus());
            }
            long itemCount = dictItemMapper.selectCount(activeQuery(ProductDictItem.class)
                .eq("dict_type_code", entity == null ? null : entity.getDictTypeCode()));
            if (itemCount > 0) {
                throw ServiceException.ofMessageKey("product.dict.typeHasItems");
            }
        }
        return remove(dictTypeMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        ProductDictType current = dictTypeMapper.selectById(id);
        if (current != null) {
            assertDictEditable(current.getSystemFlag(), current.getEditableFlag());
        }
        return dictTypeMapper.update(null, new LambdaUpdateWrapper<ProductDictType>()
            .eq(ProductDictType::getDictTypeId, id)
            .set(ProductDictType::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductDictType entity = dictTypeMapper.selectById(id);
        if (entity == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        ReferenceCheckResultVo references = checkReferences(id);
        if (isDictLocked(entity.getSystemFlag(), entity.getEditableFlag())) {
            return deniedEditCheck(entity.getStatus(), "product.dict.notEditable", references);
        }
        return editCheckResult(entity.getStatus(), references);
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        ProductDictType entity = dictTypeMapper.selectById(id);
        if (entity == null || StringUtils.isBlank(entity.getDictTypeCode())) {
            return referenceResult(0, null, null);
        }
        long count = dictItemMapper.selectCount(activeQuery(ProductDictItem.class).eq("dict_type_code", entity.getDictTypeCode()));
        return referenceResult(count, "product.dict.typeHasItems", "Dictionary items: " + count);
    }

    private QueryWrapper<ProductDictType> buildQueryWrapper(ProductDictTypeBo bo) {
        QueryWrapper<ProductDictType> q = activeQuery(ProductDictType.class);
        if (bo != null) {
            like(q, "dict_type_code", bo.getDictTypeCode());
            if (StringUtils.isNotBlank(bo.getDictTypeNameCn())) {
                q.and(wrapper -> wrapper.like("dict_type_name_cn", bo.getDictTypeNameCn()).or().like("dict_type_name_en", bo.getDictTypeNameCn()));
            }
            eq(q, "business_domain", bo.getBusinessDomain());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void validateDictType(ProductDictTypeBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getDictTypeCode())) {
            throw ServiceException.ofMessageKey("product.dict.typeCodeRequired");
        }
        QueryWrapper<ProductDictType> q = activeQuery(ProductDictType.class)
            .eq("dict_type_code", bo.getDictTypeCode());
        if (bo.getDictTypeId() != null) {
            q.ne("dict_type_id", bo.getDictTypeId());
        }
        if (dictTypeMapper.selectCount(q) > 0) {
            throw ServiceException.ofMessageKey("product.dict.typeCodeExists");
        }
    }

    private void assertDictEditable(Boolean systemFlag, Boolean editableFlag) {
        if (isDictLocked(systemFlag, editableFlag)) {
            throw ServiceException.ofMessageKey("product.dict.notEditable");
        }
    }

    private boolean isDictLocked(Boolean systemFlag, Boolean editableFlag) {
        return Boolean.TRUE.equals(systemFlag) && Boolean.FALSE.equals(editableFlag);
    }

}
