package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMaterialAttributeBo;
import com.bocoo.product.domain.entity.ProductMaterialAttribute;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMaterialAttributeMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductMaterialAttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMaterialAttributeServiceImpl extends ProductServiceSupport implements ProductMaterialAttributeService {

    private final ProductMaterialAttributeMapper materialAttributeMapper;

    @Override
    public TableDataInfo<ProductMaterialAttributeVo> queryPageList(ProductMaterialAttributeBo bo, PageQuery pageQuery) {
        return page(materialAttributeMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ProductMaterialAttributeVo> queryList(ProductMaterialAttributeBo bo) {
        return materialAttributeMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductMaterialAttributeVo queryById(Long id) {
        return materialAttributeMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductMaterialAttributeBo bo) {
        ProductMaterialAttribute entity = MapstructUtils.convert(bo, ProductMaterialAttribute.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return materialAttributeMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductMaterialAttributeBo bo) {
        ProductMaterialAttribute entity = MapstructUtils.convert(bo, ProductMaterialAttribute.class);
        return entity != null && materialAttributeMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(materialAttributeMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return materialAttributeMapper.update(null, new LambdaUpdateWrapper<ProductMaterialAttribute>()
            .eq(ProductMaterialAttribute::getAttributeValueId, id)
            .set(ProductMaterialAttribute::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        return referenceResult(0, null, null);
    }

    private QueryWrapper<ProductMaterialAttribute> buildQueryWrapper(ProductMaterialAttributeBo bo) {
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
