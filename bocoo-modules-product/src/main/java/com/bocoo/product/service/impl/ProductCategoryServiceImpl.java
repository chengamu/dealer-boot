package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.vo.ProductCategoryVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.service.ProductCategoryService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl extends ProductServiceSupport implements ProductCategoryService {

    private final ProductCategoryMapper categoryMapper;

    @Override
    public TableDataInfo<ProductCategoryVo> queryPageList(ProductCategoryBo bo, PageQuery pageQuery) {
        return page(categoryMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ProductCategoryVo> queryList(ProductCategoryBo bo) {
        return categoryMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductCategoryVo queryById(Long id) {
        return categoryMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductCategoryBo bo) {
        ProductCategory entity = MapstructUtils.convert(bo, ProductCategory.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return categoryMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductCategoryBo bo) {
        ProductCategory entity = MapstructUtils.convert(bo, ProductCategory.class);
        return entity != null && categoryMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(categoryMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return categoryMapper.update(null, new LambdaUpdateWrapper<ProductCategory>()
            .eq(ProductCategory::getCategoryId, id)
            .set(ProductCategory::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long categoryId) {
        return referenceResult(0, null, null);
    }

    private QueryWrapper<ProductCategory> buildQueryWrapper(ProductCategoryBo bo) {
        QueryWrapper<ProductCategory> q = activeQuery(ProductCategory.class);
        if (bo != null) {
            like(q, "category_code", bo.getCategoryCode());
            if (StringUtils.isNotBlank(bo.getCategoryNameCn())) {
                q.and(wrapper -> wrapper.like("category_name_cn", bo.getCategoryNameCn()).or().like("category_name_en", bo.getCategoryNameCn()));
            }
            like(q, "category_name_en", bo.getCategoryNameEn());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "category_id");
    }
}
