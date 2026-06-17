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
        return page(baseAttributeMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ProductBaseAttributeVo> queryList(ProductBaseAttributeBo bo) {
        return baseAttributeMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductBaseAttributeVo queryById(Long id) {
        return baseAttributeMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductBaseAttributeBo bo) {
        normalizeBaseAttribute(bo);
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
        ProductBaseAttribute entity = MapstructUtils.convert(bo, ProductBaseAttribute.class);
        return entity != null && baseAttributeMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(baseAttributeMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return baseAttributeMapper.update(null, new LambdaUpdateWrapper<ProductBaseAttribute>()
            .eq(ProductBaseAttribute::getAttributeId, id)
            .set(ProductBaseAttribute::getStatus, status)) > 0;
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
        return q.orderByAsc("sort_order", "attribute_id");
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
}
