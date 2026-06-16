package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductUnitBo;
import com.bocoo.product.domain.entity.FabricProfile;
import com.bocoo.product.domain.entity.ProductComponent;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductUnit;
import com.bocoo.product.domain.vo.ProductUnitVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.FabricProfileMapper;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.mapper.ProductComponentMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
import com.bocoo.product.mapper.ProductUnitMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductUnitServiceImpl extends ProductServiceSupport implements ProductUnitService {

    private final ProductUnitMapper unitMapper;
    private final ProductMaterialMapper materialMapper;
    private final ProductComponentMapper componentMapper;
    private final ProductComponentItemMapper componentItemMapper;
    private final FabricProfileMapper fabricProfileMapper;

    @Override
    public TableDataInfo<ProductUnitVo> queryPageList(ProductUnitBo bo, PageQuery pageQuery) {
        return page(unitMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ProductUnitVo> queryList(ProductUnitBo bo) {
        return unitMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductUnitVo queryById(Long id) {
        return unitMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductUnitBo bo) {
        ProductUnit entity = MapstructUtils.convert(bo, ProductUnit.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return unitMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductUnitBo bo) {
        ProductUnit entity = MapstructUtils.convert(bo, ProductUnit.class);
        return entity != null && unitMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(unitMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return unitMapper.update(null, new LambdaUpdateWrapper<ProductUnit>()
            .eq(ProductUnit::getUnitId, id)
            .set(ProductUnit::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long unitId) {
        ProductUnit unit = unitMapper.selectById(unitId);
        if (unit == null || unit.getUnitCode() == null) {
            return referenceResult(0, null, null);
        }
        String code = unit.getUnitCode();
        long count = materialMapper.selectCount(activeQuery(ProductMaterial.class).eq("unit_code", code))
            + componentMapper.selectCount(activeQuery(ProductComponent.class).eq("unit_code", code))
            + componentItemMapper.selectCount(activeQuery(ProductComponentItem.class).eq("unit_code", code))
            + fabricProfileMapper.selectCount(activeQuery(FabricProfile.class)
            .and(q -> q.eq("width_unit", code).or().eq("thickness_unit", code)
                .or().eq("purchase_unit_code", code).or().eq("inventory_unit_code", code).or().eq("sales_unit_code", code)));
        return referenceResult(count, "product.unit.hasReferences", "Unit code references: " + count);
    }

    private QueryWrapper<ProductUnit> buildQueryWrapper(ProductUnitBo bo) {
        QueryWrapper<ProductUnit> q = activeQuery(ProductUnit.class);
        if (bo != null) {
            like(q, "unit_code", bo.getUnitCode());
            if (StringUtils.isNotBlank(bo.getUnitNameCn())) {
                q.and(wrapper -> wrapper.like("unit_name_cn", bo.getUnitNameCn()).or().like("unit_name_en", bo.getUnitNameCn()));
            }
            eq(q, "unit_type", bo.getUnitType());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "unit_id");
    }
}
