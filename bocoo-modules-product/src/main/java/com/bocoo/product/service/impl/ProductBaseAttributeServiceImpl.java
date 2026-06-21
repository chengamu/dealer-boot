package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductBaseAttributeBo;
import com.bocoo.product.domain.entity.ProductBaseAttribute;
import com.bocoo.product.domain.entity.ProductMaterialAttribute;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductBaseAttributeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductBaseAttributeMapper;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.service.ProductBaseAttributeService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductBaseAttributeServiceImpl extends ProductServiceSupport implements ProductBaseAttributeService {

    private final ProductBaseAttributeMapper baseAttributeMapper;
    private final ProductMaterialAttributeMapper materialAttributeMapper;

    @Override
    public TableDataInfo<ProductBaseAttributeVo> queryPageList(ProductBaseAttributeBo bo, PageQuery pageQuery) {
        return page(baseAttributeMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "attribute_id"));
    }

    @Override
    public List<ProductBaseAttributeVo> queryList(ProductBaseAttributeBo bo) {
        return baseAttributeMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "attribute_id")));
    }

    @Override
    public ProductBaseAttributeVo queryById(Long id) {
        return baseAttributeMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductBaseAttributeBo bo) {
        normalizeBaseAttribute(bo);
        validateBaseAttributeUnique(bo);
        ProductBaseAttribute entity = MapstructUtils.convert(bo, ProductBaseAttribute.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return baseAttributeMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductBaseAttributeBo bo) {
        normalizeBaseAttribute(bo);
        validateBaseAttributeUnique(bo);
        if (bo != null && bo.getAttributeId() != null) {
            ProductBaseAttribute current = baseAttributeMapper.selectById(bo.getAttributeId());
            if (current != null) {
                assertNormalEditable(current.getStatus());
            }
        }
        ProductBaseAttribute entity = MapstructUtils.convert(bo, ProductBaseAttribute.class);
        return entity != null && baseAttributeMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            assertNoReferences(checkReferences(id));
        }
        return remove(baseAttributeMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return baseAttributeMapper.update(null, new LambdaUpdateWrapper<ProductBaseAttribute>()
            .eq(ProductBaseAttribute::getAttributeId, id)
            .set(ProductBaseAttribute::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductBaseAttribute attribute = baseAttributeMapper.selectById(id);
        if (attribute == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        return editCheckResult(attribute.getStatus(), checkReferences(id));
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long attributeId) {
        long count = materialAttributeMapper.selectCount(activeQuery(ProductMaterialAttribute.class).eq("attribute_id", attributeId));
        return referenceResult(count, "product.baseAttribute.hasReferences", "Material attributes: " + count);
    }

    private QueryWrapper<ProductBaseAttribute> buildQueryWrapper(ProductBaseAttributeBo bo) {
        QueryWrapper<ProductBaseAttribute> q = activeQuery(ProductBaseAttribute.class);
        if (bo != null) {
            eq(q, "attribute_group", bo.getAttributeGroup());
            like(q, "attribute_code", bo.getAttributeCode());
            if (StringUtils.isNotBlank(bo.getAttributeNameCn())) {
                q.and(wrapper -> wrapper.like("attribute_name_cn", bo.getAttributeNameCn()).or().like("attribute_name_en", bo.getAttributeNameCn()));
            }
            like(q, "material_types", bo.getMaterialTypes());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalizeBaseAttribute(ProductBaseAttributeBo bo) {
        if (bo == null) {
            return;
        }
        if (StringUtils.isBlank(bo.getAttributeGroup())) {
            throw ServiceException.ofMessageKey("product.baseAttribute.groupRequired");
        }
        if (StringUtils.isBlank(bo.getValueType())) {
            bo.setValueType("TEXT");
        } else {
            bo.setValueType(bo.getValueType().toUpperCase());
        }
        if (!"NUMBER".equals(bo.getValueType())) {
            bo.setUnitCode(null);
        }
    }

    private void validateBaseAttributeUnique(ProductBaseAttributeBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getAttributeGroup()) || StringUtils.isBlank(bo.getAttributeCode())) {
            return;
        }
        long count = baseAttributeMapper.selectCount(activeQuery(ProductBaseAttribute.class)
            .eq("attribute_group", bo.getAttributeGroup())
            .eq("attribute_code", bo.getAttributeCode())
            .ne(bo.getAttributeId() != null, "attribute_id", bo.getAttributeId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.baseAttribute.codeExists");
        }
    }
}
