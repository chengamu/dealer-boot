package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.SalesProductBo;
import com.bocoo.product.domain.entity.ConfigTemplate;
import com.bocoo.product.domain.entity.ConfigTemplateVersion;
import com.bocoo.product.domain.entity.ProductCategory;
import com.bocoo.product.domain.entity.SalesProduct;
import com.bocoo.product.domain.vo.SalesProductVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ConfigTemplateMapper;
import com.bocoo.product.mapper.ConfigTemplateVersionMapper;
import com.bocoo.product.mapper.ProductCategoryMapper;
import com.bocoo.product.mapper.SalesProductMapper;
import com.bocoo.product.service.SalesProductService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SalesProductServiceImpl extends ProductServiceSupport implements SalesProductService {

    private final SalesProductMapper salesProductMapper;
    private final ProductCategoryMapper categoryMapper;

    private final ConfigTemplateMapper templateMapper;

    private final ConfigTemplateVersionMapper templateVersionMapper;

    @Override
    public TableDataInfo<SalesProductVo> queryPageList(SalesProductBo bo, PageQuery pageQuery) {
        return page(salesProductMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "sales_product_id"));
    }

    @Override
    public List<SalesProductVo> queryList(SalesProductBo bo) {
        return salesProductMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "sales_product_id")));
    }

    @Override
    public SalesProductVo queryById(Long id) {
        return salesProductMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(SalesProductBo bo) {
        SalesProduct entity = MapstructUtils.convert(bo, SalesProduct.class);
        if (entity == null) { return Boolean.FALSE; }
        syncCategorySnapshot(entity);
        ProductEntityDefaults.prepareInsert(entity);
        return salesProductMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(SalesProductBo bo) {
        SalesProduct entity = MapstructUtils.convert(bo, SalesProduct.class);
        if (entity == null) { return Boolean.FALSE; }
        syncCategorySnapshot(entity);
        return salesProductMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(salesProductMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return salesProductMapper.update(null, new LambdaUpdateWrapper<SalesProduct>()
            .eq(SalesProduct::getSalesProductId, id)
            .set(SalesProduct::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long id) {
        SalesProduct product = salesProductMapper.selectById(id);
        if (product == null) { return referenceResult(0, null, null); }
        long count = templateMapper.selectCount(activeQuery(ConfigTemplate.class).eq("sales_product_id", id))
            + templateVersionMapper.selectCount(activeQuery(ConfigTemplateVersion.class).eq("sales_product_id", id));
        return referenceResult(count, "product.salesProduct.hasReferences", "Config templates / versions: " + count);
    }

    private QueryWrapper<SalesProduct> buildQueryWrapper(SalesProductBo bo) {
        QueryWrapper<SalesProduct> q = activeQuery(SalesProduct.class);
        if (bo != null) {
        like(q, "sales_product_code", bo.getSalesProductCode());
        if (StringUtils.isNotBlank(bo.getSalesProductNameCn())) {
            q.and(wrapper -> wrapper.like("sales_product_name_cn", bo.getSalesProductNameCn()).or().like("sales_product_name_en", bo.getSalesProductNameCn()));
        }
        like(q, "sales_product_name_en", bo.getSalesProductNameEn());
        eq(q, "category_id", bo.getCategoryId());
        eq(q, "category_code", bo.getCategoryCode());
        eq(q, "product_type", bo.getProductType());
        eq(q, "sales_mode", bo.getSalesMode());
        eq(q, "template_code", bo.getTemplateCode());
        eq(q, "biz_status", bo.getBizStatus());
        eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void syncCategorySnapshot(SalesProduct product) {
        if (product.getCategoryId() == null) { return; }
        ProductCategory category = categoryMapper.selectById(product.getCategoryId());
        if (category == null) { return; }
        product.setCategoryCode(category.getCategoryCode());
        product.setCategoryNameCn(category.getCategoryNameCn());
        product.setCategoryNameEn(category.getCategoryNameEn());
    }
}
