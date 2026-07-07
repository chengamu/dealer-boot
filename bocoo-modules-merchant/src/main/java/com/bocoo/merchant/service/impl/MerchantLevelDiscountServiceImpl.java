package com.bocoo.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.merchant.domain.bo.MerchantLevelDiscountBo;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import com.bocoo.merchant.domain.entity.MerchantLevelDiscount;
import com.bocoo.merchant.domain.vo.MerchantLevelDiscountVo;
import com.bocoo.merchant.mapper.MerchantLevelDiscountMapper;
import com.bocoo.merchant.mapper.MerchantLevelMapper;
import com.bocoo.merchant.service.MerchantLevelDiscountService;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.ProductDictItem;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.ProductDictItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantLevelDiscountServiceImpl extends MerchantServiceSupport implements MerchantLevelDiscountService {

    private static final String PRODUCT_TYPE_DICT = "product_type";

    private final MerchantLevelDiscountMapper discountMapper;
    private final MerchantLevelMapper levelMapper;
    private final ProductCategoryMapper categoryMapper;
    private final ProductDictItemMapper dictItemMapper;

    @Override
    public TableDataInfo<MerchantLevelDiscountVo> queryPageList(MerchantLevelDiscountBo bo, PageQuery pageQuery) {
        checkPlatformTenant();
        return page(discountMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "discount_id"));
    }

    @Override
    public List<MerchantLevelDiscountVo> queryList(MerchantLevelDiscountBo bo) {
        checkPlatformTenant();
        return discountMapper.selectVoList(buildQueryWrapper(bo).orderByAsc("sort_order", "discount_id"));
    }

    @Override
    public MerchantLevelDiscountVo queryById(Long id) {
        checkPlatformTenant();
        return discountMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(MerchantLevelDiscountBo bo) {
        checkPlatformTenant();
        normalize(bo);
        validateNaturalUnique(bo);
        MerchantLevelDiscount entity = MapstructUtils.convert(bo, MerchantLevelDiscount.class);
        return entity != null && discountMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(MerchantLevelDiscountBo bo) {
        checkPlatformTenant();
        normalize(bo);
        validateNaturalUnique(bo);
        MerchantLevelDiscount current = discountMapper.selectById(bo.getDiscountId());
        if (current == null || !DEL_FLAG_NORMAL.equals(current.getDelFlag())) {
            throw ServiceException.ofMessageKey("merchant.discount.notFound");
        }
        if (enabled(current.getStatus())) {
            throw ServiceException.ofMessageKey("merchant.discount.edit.enabledDenied");
        }
        MerchantLevelDiscount entity = MapstructUtils.convert(bo, MerchantLevelDiscount.class);
        return entity != null && discountMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        checkPlatformTenant();
        for (Long id : ids) {
            MerchantLevelDiscount current = discountMapper.selectById(id);
            if (current != null && enabled(current.getStatus())) {
                throw ServiceException.ofMessageKey("merchant.discount.delete.enabledDenied");
            }
        }
        return discountMapper.deleteBatchIds(List.of(ids)) > 0;
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        checkPlatformTenant();
        String normalizedStatus = normalizeStatus(status);
        return discountMapper.update(null, new LambdaUpdateWrapper<MerchantLevelDiscount>()
            .eq(MerchantLevelDiscount::getDiscountId, id)
            .set(MerchantLevelDiscount::getStatus, normalizedStatus)) > 0;
    }

    private QueryWrapper<MerchantLevelDiscount> buildQueryWrapper(MerchantLevelDiscountBo bo) {
        QueryWrapper<MerchantLevelDiscount> q = this.<MerchantLevelDiscount>activeQuery();
        if (bo != null) {
            eq(q, "level_id", bo.getLevelId());
            like(q, "level_name", bo.getLevelName());
            eq(q, "category_id", bo.getCategoryId());
            eq(q, "product_type_code", bo.getProductTypeCode());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void normalize(MerchantLevelDiscountBo bo) {
        bo.setTenantId(PLATFORM_TENANT_ID);
        bo.setProductTypeCode(StringUtils.trimToEmpty(bo.getProductTypeCode()).toUpperCase());
        MerchantLevel level = levelMapper.selectOne(this.<MerchantLevel>activeQuery()
            .eq("level_id", bo.getLevelId())
            .eq("status", STATUS_ENABLED), false);
        if (level == null) {
            throw ServiceException.ofMessageKey("merchant.level.notFound");
        }
        ProductCategory category = categoryMapper.selectOne(new QueryWrapper<ProductCategory>()
            .eq("del_flag", DEL_FLAG_NORMAL)
            .eq("status", STATUS_ENABLED)
            .eq("category_id", bo.getCategoryId()), false);
        if (category == null) {
            throw ServiceException.ofMessageKey("merchant.discount.category.invalid");
        }
        ProductDictItem productType = dictItemMapper.selectOne(new QueryWrapper<ProductDictItem>()
            .eq("del_flag", DEL_FLAG_NORMAL)
            .eq("status", STATUS_ENABLED)
            .eq("dict_type_code", PRODUCT_TYPE_DICT)
            .eq("dict_item_value", bo.getProductTypeCode()), false);
        if (productType == null) {
            throw ServiceException.ofMessageKey("merchant.discount.productType.invalid");
        }
        bo.setLevelCode(level.getLevelCode());
        bo.setLevelName(level.getLevelName());
        bo.setCategoryCode(category.getCategoryCode());
        bo.setCategoryNameCn(category.getCategoryNameCn());
        bo.setProductTypeNameCn(productType.getDictItemLabelCn());
        if (StringUtils.isBlank(bo.getStatus())) {
            bo.setStatus(STATUS_DISABLED);
        } else {
            bo.setStatus(normalizeStatus(bo.getStatus()));
        }
        bo.setDelFlag(DEL_FLAG_NORMAL);
    }

    private void validateNaturalUnique(MerchantLevelDiscountBo bo) {
        long count = discountMapper.selectCount(this.<MerchantLevelDiscount>activeQuery()
            .eq("level_id", bo.getLevelId())
            .eq("category_id", bo.getCategoryId())
            .eq("product_type_code", bo.getProductTypeCode())
            .ne(bo.getDiscountId() != null, "discount_id", bo.getDiscountId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("merchant.discount.exists");
        }
    }
}
