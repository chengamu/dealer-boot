package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductComponentItemBo;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.vo.ProductComponentItemVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.service.ProductComponentItemService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductComponentItemServiceImpl extends ProductServiceSupport implements ProductComponentItemService {

    private final ProductComponentItemMapper componentItemMapper;

    @Override
    public TableDataInfo<ProductComponentItemVo> queryPageList(ProductComponentItemBo bo, PageQuery pageQuery) {
        return page(componentItemMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ProductComponentItemVo> queryList(ProductComponentItemBo bo) {
        return componentItemMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductComponentItemVo queryById(Long id) {
        return componentItemMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductComponentItemBo bo) {
        ProductComponentItem entity = MapstructUtils.convert(bo, ProductComponentItem.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return componentItemMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductComponentItemBo bo) {
        ProductComponentItem entity = MapstructUtils.convert(bo, ProductComponentItem.class);
        return entity != null && componentItemMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(componentItemMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return componentItemMapper.update(null, new LambdaUpdateWrapper<ProductComponentItem>()
            .eq(ProductComponentItem::getComponentItemId, id)
            .set(ProductComponentItem::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        return referenceResult(0, null, null);
    }

    private QueryWrapper<ProductComponentItem> buildQueryWrapper(ProductComponentItemBo bo) {
        QueryWrapper<ProductComponentItem> q = activeQuery(ProductComponentItem.class);
        if (bo != null) {
            eq(q, "component_id", bo.getComponentId());
            like(q, "component_code", bo.getComponentCode());
            eq(q, "material_id", bo.getMaterialId());
            like(q, "material_code", bo.getMaterialCode());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "component_item_id");
    }
}
