package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductComponentBo;
import com.bocoo.product.domain.entity.ProductComponent;
import com.bocoo.product.domain.entity.ProductComponentItem;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.ProductComponentVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductComponentItemMapper;
import com.bocoo.product.mapper.ProductComponentMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.ProductComponentService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductComponentServiceImpl extends ProductServiceSupport implements ProductComponentService {

    private final ProductComponentMapper componentMapper;
    private final ProductComponentItemMapper componentItemMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    @Override
    public TableDataInfo<ProductComponentVo> queryPageList(ProductComponentBo bo, PageQuery pageQuery) {
        return page(componentMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ProductComponentVo> queryList(ProductComponentBo bo) {
        return componentMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductComponentVo queryById(Long id) {
        return componentMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductComponentBo bo) {
        ProductComponent entity = MapstructUtils.convert(bo, ProductComponent.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return componentMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductComponentBo bo) {
        ProductComponent entity = MapstructUtils.convert(bo, ProductComponent.class);
        return entity != null && componentMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(componentMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return componentMapper.update(null, new LambdaUpdateWrapper<ProductComponent>()
            .eq(ProductComponent::getComponentId, id)
            .set(ProductComponent::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long componentId) {
        long count = componentItemMapper.selectCount(activeQuery(ProductComponentItem.class).eq("component_id", componentId))
            + mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "COMPONENT").eq("target_id", componentId));
        return referenceResult(count, "product.component.hasReferences", "Component references: " + count);
    }

    private QueryWrapper<ProductComponent> buildQueryWrapper(ProductComponentBo bo) {
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
}
