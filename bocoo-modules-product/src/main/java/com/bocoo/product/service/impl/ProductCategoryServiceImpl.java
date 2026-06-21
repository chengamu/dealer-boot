package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.entity.SalesProduct;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductCategoryVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.mapper.SalesProductMapper;
import com.bocoo.product.service.ProductCategoryService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl extends ProductServiceSupport implements ProductCategoryService {

    private final ProductCategoryMapper categoryMapper;
    private final SalesProductMapper salesProductMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    @Override
    public TableDataInfo<ProductCategoryVo> queryPageList(ProductCategoryBo bo, PageQuery pageQuery) {
        return page(categoryMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "category_id"));
    }

    @Override
    public List<ProductCategoryVo> queryList(ProductCategoryBo bo) {
        return categoryMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "category_id")));
    }

    @Override
    public ProductCategoryVo queryById(Long id) {
        return categoryMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductCategoryBo bo) {
        normalizeCategory(bo);
        validateCategoryCodeUnique(bo);
        ProductCategory entity = MapstructUtils.convert(bo, ProductCategory.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return categoryMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductCategoryBo bo) {
        normalizeCategory(bo);
        validateCategoryCodeUnique(bo);
        if (bo != null && bo.getCategoryId() != null) {
            ProductCategory current = categoryMapper.selectById(bo.getCategoryId());
            if (current != null) {
                assertNormalEditable(current.getStatus());
            }
        }
        ProductCategory entity = MapstructUtils.convert(bo, ProductCategory.class);
        return entity != null && categoryMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            assertNoReferences(checkReferences(id));
        }
        return remove(categoryMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return categoryMapper.update(null, new LambdaUpdateWrapper<ProductCategory>()
            .eq(ProductCategory::getCategoryId, id)
            .set(ProductCategory::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        ProductCategory category = categoryMapper.selectById(id);
        if (category == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        return editCheckResult(category.getStatus(), checkReferences(id));
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long categoryId) {
        ProductCategory category = categoryMapper.selectById(categoryId);
        if (category == null) {
            return referenceResult(0, null, null);
        }
        long childCount = categoryMapper.selectCount(activeQuery(ProductCategory.class).eq("parent_id", categoryId));
        long salesProductCount = salesProductMapper.selectCount(activeQuery(SalesProduct.class).eq("category_id", categoryId));
        long bindingCount = mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "CATEGORY").eq("target_id", categoryId));
        ReferenceCheckResultVo result = referenceResult(childCount + salesProductCount + bindingCount, "product.category.hasReferences", null);
        if (childCount > 0) {
            result.getReferenceSummaries().add("Child categories: " + childCount);
        }
        if (salesProductCount > 0) {
            result.getReferenceSummaries().add("Sales products: " + salesProductCount);
        }
        if (bindingCount > 0) {
            result.getReferenceSummaries().add("Media bindings: " + bindingCount);
        }
        return result;
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
        return q;
    }

    private void normalizeCategory(ProductCategoryBo bo) {
        if (bo == null) {
            return;
        }
        Long parentId = bo.getParentId();
        if (parentId == null || parentId <= 0) {
            bo.setParentId(0L);
            bo.setCategoryLevel(1);
            bo.setCategoryPath(bo.getCategoryCode());
            return;
        }
        if (parentId.equals(bo.getCategoryId())) {
            throw ServiceException.ofMessageKey("product.category.parentCannotSelf");
        }
        ProductCategory parent = categoryMapper.selectById(parentId);
        if (parent == null || !"0".equals(parent.getDelFlag())) {
            throw ServiceException.ofMessageKey("product.category.parentNotFound");
        }
        bo.setCategoryLevel((parent.getCategoryLevel() == null ? 1 : parent.getCategoryLevel()) + 1);
        String parentPath = StringUtils.blankToDefault(parent.getCategoryPath(), parent.getCategoryCode());
        bo.setCategoryPath(parentPath + "/" + bo.getCategoryCode());
    }

    private void validateCategoryCodeUnique(ProductCategoryBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getCategoryCode())) {
            return;
        }
        long count = categoryMapper.selectCount(activeQuery(ProductCategory.class)
            .eq("category_code", bo.getCategoryCode())
            .ne(bo.getCategoryId() != null, "category_id", bo.getCategoryId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.category.codeExists");
        }
    }
}
