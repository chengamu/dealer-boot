package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMediaAssetBo;
import com.bocoo.product.domain.bo.ProductMediaBindingBo;
import com.bocoo.product.domain.entity.ProductMediaAsset;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.ProductMediaAssetVo;
import com.bocoo.product.domain.vo.ProductMediaBindingVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMediaAssetMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 附件资料和资料绑定服务。
 */
@Service
@RequiredArgsConstructor
public class ProductMediaService extends ProductCrudSupport {

    private final ProductMediaAssetMapper mediaAssetMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    public TableDataInfo<ProductMediaAssetVo> queryProductMediaAssetPage(ProductMediaAssetBo bo, PageQuery pageQuery) {
        return page(mediaAssetMapper, pageQuery, mediaAssetWrapper(bo));
    }

    public List<ProductMediaAssetVo> queryProductMediaAssetList(ProductMediaAssetBo bo) {
        return mediaAssetMapper.selectVoList(mediaAssetWrapper(bo));
    }

    public ProductMediaAssetVo getProductMediaAssetById(Long id) {
        return mediaAssetMapper.selectVoById(id);
    }

    public Boolean saveProductMediaAsset(ProductMediaAssetBo bo) {
        return save(mediaAssetMapper, bo, ProductMediaAsset.class, "assetId");
    }

    public Boolean removeProductMediaAssetByIds(Long[] ids) {
        return remove(mediaAssetMapper, ids);
    }

    public Boolean updateProductMediaAssetStatus(Long id, String status) {
        return updateStatus(mediaAssetMapper, ProductMediaAsset.class, "assetId", id, status);
    }

    public ReferenceCheckResultVo checkProductMediaAssetReferences(Long assetId) {
        long count = mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("asset_id", assetId));
        return referenceResult(count, "product.mediaAsset.hasReferences", "Media bindings: " + count);
    }

    public TableDataInfo<ProductMediaBindingVo> queryProductMediaBindingPage(ProductMediaBindingBo bo, PageQuery pageQuery) {
        return page(mediaBindingMapper, pageQuery, mediaBindingWrapper(bo));
    }

    public List<ProductMediaBindingVo> queryProductMediaBindingList(ProductMediaBindingBo bo) {
        return mediaBindingMapper.selectVoList(mediaBindingWrapper(bo));
    }

    public ProductMediaBindingVo getProductMediaBindingById(Long id) {
        return mediaBindingMapper.selectVoById(id);
    }

    public Boolean saveProductMediaBinding(ProductMediaBindingBo bo) {
        return save(mediaBindingMapper, bo, ProductMediaBinding.class, "bindingId");
    }

    public Boolean removeProductMediaBindingByIds(Long[] ids) {
        return remove(mediaBindingMapper, ids);
    }

    public Boolean updateProductMediaBindingStatus(Long id, String status) {
        return updateStatus(mediaBindingMapper, ProductMediaBinding.class, "bindingId", id, status);
    }

    public ReferenceCheckResultVo checkProductMediaBindingReferences(Long bindingId) {
        return referenceResult(0, null, null);
    }

    private QueryWrapper<ProductMediaAsset> mediaAssetWrapper(ProductMediaAssetBo bo) {
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

    private QueryWrapper<ProductMediaBinding> mediaBindingWrapper(ProductMediaBindingBo bo) {
        QueryWrapper<ProductMediaBinding> q = activeQuery(ProductMediaBinding.class);
        if (bo != null) {
            like(q, "asset_code", bo.getAssetCode());
            eq(q, "target_type", bo.getTargetType());
            eq(q, "target_id", bo.getTargetId());
            like(q, "target_code", bo.getTargetCode());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByAsc("sort_order", "binding_id");
    }
}
