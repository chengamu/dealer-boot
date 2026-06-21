package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductComponentItemBo;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
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
        return page(componentItemMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "component_item_id"));
    }

    @Override
    public List<ProductComponentItemVo> queryList(ProductComponentItemBo bo) {
        return componentItemMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "component_item_id")));
    }

    @Override
    public ProductComponentItemVo queryById(Long id) {
        return componentItemMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductComponentItemBo bo) {
        validateComponentItemUnique(bo);
        ProductComponentItem entity = MapstructUtils.convert(bo, ProductComponentItem.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return componentItemMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductComponentItemBo bo) {
        validateComponentItemUnique(bo);
        if (bo != null && bo.getComponentItemId() != null) {
            ProductComponentItem current = componentItemMapper.selectById(bo.getComponentItemId());
            if (current != null) {
                assertNormalEditable(current.getStatus());
            }
        }
        ProductComponentItem entity = MapstructUtils.convert(bo, ProductComponentItem.class);
        return entity != null && componentItemMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            assertNoReferences(checkReferences(id));
        }
        return remove(componentItemMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return componentItemMapper.update(null, new LambdaUpdateWrapper<ProductComponentItem>()
            .eq(ProductComponentItem::getComponentItemId, id)
            .set(ProductComponentItem::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductComponentItem item = componentItemMapper.selectById(id);
        if (item == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        return editCheckResult(item.getStatus(), checkReferences(id));
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
        return q;
    }

    private void validateComponentItemUnique(ProductComponentItemBo bo) {
        if (bo == null) {
            return;
        }
        validateComponentMaterialUnique(bo);
        validateComponentSortUnique(bo);
    }

    private void validateComponentMaterialUnique(ProductComponentItemBo bo) {
        QueryWrapper<ProductComponentItem> q = activeQuery(ProductComponentItem.class)
            .ne(bo.getComponentItemId() != null, "component_item_id", bo.getComponentItemId());
        if (!appendComponentCondition(q, bo) || !appendMaterialCondition(q, bo)) {
            return;
        }
        if (componentItemMapper.selectCount(q) > 0) {
            throw ServiceException.ofMessageKey("product.componentItem.materialExists");
        }
    }

    private void validateComponentSortUnique(ProductComponentItemBo bo) {
        if (bo.getSortOrder() == null) {
            return;
        }
        QueryWrapper<ProductComponentItem> q = activeQuery(ProductComponentItem.class)
            .eq("sort_order", bo.getSortOrder())
            .ne(bo.getComponentItemId() != null, "component_item_id", bo.getComponentItemId());
        if (!appendComponentCondition(q, bo)) {
            return;
        }
        if (componentItemMapper.selectCount(q) > 0) {
            throw ServiceException.ofMessageKey("product.componentItem.sortExists");
        }
    }

    private boolean appendComponentCondition(QueryWrapper<ProductComponentItem> q, ProductComponentItemBo bo) {
        if (bo.getComponentId() != null) {
            q.eq("component_id", bo.getComponentId());
            return true;
        }
        if (StringUtils.isNotBlank(bo.getComponentCode())) {
            q.eq("component_code", bo.getComponentCode());
            return true;
        }
        return false;
    }

    private boolean appendMaterialCondition(QueryWrapper<ProductComponentItem> q, ProductComponentItemBo bo) {
        if (bo.getMaterialId() != null) {
            q.eq("material_id", bo.getMaterialId());
            return true;
        }
        if (StringUtils.isNotBlank(bo.getMaterialCode())) {
            q.eq("material_code", bo.getMaterialCode());
            return true;
        }
        return false;
    }
}
