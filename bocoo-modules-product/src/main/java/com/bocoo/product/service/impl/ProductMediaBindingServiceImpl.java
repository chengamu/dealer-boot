package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.service.OssService;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductMediaBindingBo;
import com.bocoo.product.domain.entity.ProductMediaAsset;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.ProductMediaBindingVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.ProductMediaAssetMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductMediaBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductMediaBindingServiceImpl extends ProductServiceSupport implements ProductMediaBindingService {

    private final ProductMediaBindingMapper mediaBindingMapper;
    private final ProductMediaAssetMapper mediaAssetMapper;
    private final OssService ossService;

    @Override
    public TableDataInfo<ProductMediaBindingVo> queryPageList(ProductMediaBindingBo bo, PageQuery pageQuery) {
        return page(mediaBindingMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "binding_id"));
    }

    @Override
    public List<ProductMediaBindingVo> queryList(ProductMediaBindingBo bo) {
        return mediaBindingMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByAsc("sort_order", "binding_id")));
    }

    @Override
    public ProductMediaBindingVo queryById(Long id) {
        return mediaBindingMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(ProductMediaBindingBo bo) {
        validateMediaBindingUnique(bo);
        ProductMediaBinding entity = MapstructUtils.convert(bo, ProductMediaBinding.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return mediaBindingMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(ProductMediaBindingBo bo) {
        validateMediaBindingUnique(bo);
        ProductMediaBinding entity = MapstructUtils.convert(bo, ProductMediaBinding.class);
        return entity != null && mediaBindingMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithValidByIds(Long[] ids) {
        List<ProductMediaBinding> bindings = mediaBindingMapper.selectBatchIds(Arrays.asList(ids));
        boolean removed = remove(mediaBindingMapper, ids);
        if (!removed) return Boolean.FALSE;
        Set<Long> assetIds = bindings.stream()
            .map(ProductMediaBinding::getAssetId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        assetIds.forEach(this::cleanupUnusedAsset);
        return Boolean.TRUE;
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return mediaBindingMapper.update(null, new LambdaUpdateWrapper<ProductMediaBinding>()
            .eq(ProductMediaBinding::getBindingId, id)
            .set(ProductMediaBinding::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long bindingId) {
        return referenceResult(0, null, null);
    }

    private QueryWrapper<ProductMediaBinding> buildQueryWrapper(ProductMediaBindingBo bo) {
        QueryWrapper<ProductMediaBinding> q = activeQuery(ProductMediaBinding.class);
        if (bo != null) {
            like(q, "asset_code", bo.getAssetCode());
            eq(q, "target_type", bo.getTargetType());
            eq(q, "target_id", bo.getTargetId());
            like(q, "target_code", bo.getTargetCode());
            eq(q, "status", bo.getStatus());
        }
        return q;
    }

    private void validateMediaBindingUnique(ProductMediaBindingBo bo) {
        if (bo == null || bo.getAssetId() == null || bo.getTargetId() == null || StringUtils.isBlank(bo.getTargetType())) {
            return;
        }
        QueryWrapper<ProductMediaBinding> q = activeQuery(ProductMediaBinding.class)
            .eq("asset_id", bo.getAssetId())
            .eq("target_type", bo.getTargetType())
            .eq("target_id", bo.getTargetId())
            .ne(bo.getBindingId() != null, "binding_id", bo.getBindingId());
        eqOrNull(q, "usage_type", bo.getUsageType());
        eqOrNull(q, "language_code", bo.getLanguageCode());
        if (mediaBindingMapper.selectCount(q) > 0) {
            throw ServiceException.ofMessageKey("product.mediaBinding.targetExists");
        }
    }

    private void eqOrNull(QueryWrapper<ProductMediaBinding> q, String column, String value) {
        if (StringUtils.isBlank(value)) {
            q.isNull(column);
            return;
        }
        q.eq(column, value);
    }

    private void cleanupUnusedAsset(Long assetId) {
        long references = mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("asset_id", assetId));
        if (references > 0) return;
        ProductMediaAsset asset = mediaAssetMapper.selectById(assetId);
        if (asset == null || mediaAssetMapper.deleteById(assetId) <= 0 || asset.getOssId() == null) return;
        try {
            ossService.deleteByIds(List.of(asset.getOssId()));
        } catch (RuntimeException exception) {
            log.warn("Failed to cleanup material attachment OSS file: {}", asset.getOssId(), exception);
        }
    }
}
