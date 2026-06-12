package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductComponentBo;
import com.bocoo.product.domain.bo.ProductComponentItemBo;
import com.bocoo.product.domain.entity.ProductComponent;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.ProductComponentItemVo;
import com.bocoo.product.domain.vo.ProductComponentVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.mapper.ProductComponentMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 组件包和组件明细服务。
 */
@Service
@RequiredArgsConstructor
public class ProductComponentService extends ProductCrudSupport {

    private final ProductComponentMapper componentMapper;
    private final ProductComponentItemMapper componentItemMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    public TableDataInfo<ProductComponentVo> queryProductComponentPage(ProductComponentBo bo, PageQuery pageQuery) {
        return page(componentMapper, pageQuery, componentWrapper(bo));
    }

    public List<ProductComponentVo> queryProductComponentList(ProductComponentBo bo) {
        return componentMapper.selectVoList(componentWrapper(bo));
    }

    public ProductComponentVo getProductComponentById(Long id) {
        return componentMapper.selectVoById(id);
    }

    public Boolean saveProductComponent(ProductComponentBo bo) {
        return save(componentMapper, bo, ProductComponent.class, "componentId");
    }

    public Boolean removeProductComponentByIds(Long[] ids) {
        return remove(componentMapper, ids);
    }

    public Boolean updateProductComponentStatus(Long id, String status) {
        return updateStatus(componentMapper, ProductComponent.class, "componentId", id, status);
    }

    public ReferenceCheckResultVo checkProductComponentReferences(Long componentId) {
        long count = componentItemMapper.selectCount(activeQuery(ProductComponentItem.class).eq("component_id", componentId))
            + mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "COMPONENT").eq("target_id", componentId));
        return referenceResult(count, "product.component.hasReferences", "Component references: " + count);
    }

    public TableDataInfo<ProductComponentItemVo> queryProductComponentItemPage(ProductComponentItemBo bo, PageQuery pageQuery) {
        return page(componentItemMapper, pageQuery, componentItemWrapper(bo));
    }

    public List<ProductComponentItemVo> queryProductComponentItemList(ProductComponentItemBo bo) {
        return componentItemMapper.selectVoList(componentItemWrapper(bo));
    }

    public ProductComponentItemVo getProductComponentItemById(Long id) {
        return componentItemMapper.selectVoById(id);
    }

    public Boolean saveProductComponentItem(ProductComponentItemBo bo) {
        return save(componentItemMapper, bo, ProductComponentItem.class, "componentItemId");
    }

    public Boolean removeProductComponentItemByIds(Long[] ids) {
        return remove(componentItemMapper, ids);
    }

    public Boolean updateProductComponentItemStatus(Long id, String status) {
        return updateStatus(componentItemMapper, ProductComponentItem.class, "componentItemId", id, status);
    }

    public ReferenceCheckResultVo checkProductComponentItemReferences(Long id) {
        return referenceResult(0, null, null);
    }

    private QueryWrapper<ProductComponent> componentWrapper(ProductComponentBo bo) {
        QueryWrapper<ProductComponent> q = activeQuery(ProductComponent.class);
        if (bo != null) {
            like(q, "component_code", bo.getComponentCode());
            if (StringUtils.isNotBlank(bo.getComponentNameCn())) {
                q.and(wrapper -> wrapper.like("component_name_cn", bo.getComponentNameCn()).or().like("component_name_en", bo.getComponentNameCn()));
            }
            eq(q, "component_type", bo.getComponentType());
            like(q, "material_code", bo.getMaterialCode());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByDesc("update_time");
    }

    private QueryWrapper<ProductComponentItem> componentItemWrapper(ProductComponentItemBo bo) {
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
