package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMediaAssetBo;
import com.bocoo.product.domain.entity.ProductMediaAsset;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.ProductMediaAssetVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMediaAssetMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductMediaAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductMediaAssetServiceImpl extends ProductServiceSupport implements ProductMediaAssetService {

    private final ProductMediaAssetMapper mediaAssetMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    @Override
    public TableDataInfo<ProductMediaAssetVo> queryPageList(ProductMediaAssetBo bo, PageQuery pageQuery) {
        return page(mediaAssetMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<ProductMediaAssetVo> queryList(ProductMediaAssetBo bo) {
        return mediaAssetMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductMediaAssetVo queryById(Long id) {
        return mediaAssetMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductMediaAssetBo bo) {
        ProductMediaAsset entity = MapstructUtils.convert(bo, ProductMediaAsset.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return mediaAssetMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductMediaAssetBo bo) {
        ProductMediaAsset entity = MapstructUtils.convert(bo, ProductMediaAsset.class);
        return entity != null && mediaAssetMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(mediaAssetMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return mediaAssetMapper.update(null, new LambdaUpdateWrapper<ProductMediaAsset>()
            .eq(ProductMediaAsset::getAssetId, id)
            .set(ProductMediaAsset::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long assetId) {
        long count = mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("asset_id", assetId));
        return referenceResult(count, "product.mediaAsset.hasReferences", "Media bindings: " + count);
    }

    private QueryWrapper<ProductMediaAsset> buildQueryWrapper(ProductMediaAssetBo bo) {
        QueryWrapper<ProductMediaAsset> q = activeQuery(ProductMediaAsset.class);
        if (bo != null) {
            like(q, "asset_code", bo.getAssetCode());
            if (StringUtils.isNotBlank(bo.getAssetNameCn())) {
                q.and(wrapper -> wrapper.like("asset_name_cn", bo.getAssetNameCn()).or().like("asset_name_en", bo.getAssetNameCn()));
            }
            eq(q, "asset_type", bo.getAssetType());
            eq(q, "usage_type", bo.getUsageType());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByDesc("update_time");
    }
}
