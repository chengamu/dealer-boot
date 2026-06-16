package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.FabricSeriesBo;
import com.bocoo.product.domain.entity.FabricProfile;
import com.bocoo.product.domain.entity.FabricSeries;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.FabricSeriesVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.FabricProfileMapper;
import com.bocoo.product.mapper.FabricSeriesMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.FabricSeriesService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FabricSeriesServiceImpl extends ProductServiceSupport implements FabricSeriesService {

    private final FabricSeriesMapper fabricSeriesMapper;
    private final FabricProfileMapper fabricProfileMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    @Override
    public TableDataInfo<FabricSeriesVo> queryPageList(FabricSeriesBo bo, PageQuery pageQuery) {
        return page(fabricSeriesMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<FabricSeriesVo> queryList(FabricSeriesBo bo) {
        return fabricSeriesMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public FabricSeriesVo queryById(Long id) {
        return fabricSeriesMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(FabricSeriesBo bo) {
        FabricSeries entity = MapstructUtils.convert(bo, FabricSeries.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return fabricSeriesMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(FabricSeriesBo bo) {
        FabricSeries entity = MapstructUtils.convert(bo, FabricSeries.class);
        return entity != null && fabricSeriesMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(fabricSeriesMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return fabricSeriesMapper.update(null, new LambdaUpdateWrapper<FabricSeries>()
            .eq(FabricSeries::getSeriesId, id)
            .set(FabricSeries::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long seriesId) {
        long profileCount = fabricProfileMapper.selectCount(activeQuery(FabricProfile.class).eq("series_id", seriesId));
        long bindingCount = mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "FABRIC_SERIES").eq("target_id", seriesId));
        ReferenceCheckResultVo result = referenceResult(profileCount + bindingCount, "product.fabricSeries.hasReferences", null);
        if (profileCount > 0) {
            result.getReferenceSummaries().add("Fabric profiles: " + profileCount);
        }
        if (bindingCount > 0) {
            result.getReferenceSummaries().add("Media bindings: " + bindingCount);
        }
        return result;
    }

    private QueryWrapper<FabricSeries> buildQueryWrapper(FabricSeriesBo bo) {
        QueryWrapper<FabricSeries> q = activeQuery(FabricSeries.class);
        if (bo != null) {
            like(q, "series_code", bo.getSeriesCode());
            if (StringUtils.isNotBlank(bo.getSeriesNameCn())) {
                q.and(wrapper -> wrapper.like("series_name_cn", bo.getSeriesNameCn()).or().like("series_name_en", bo.getSeriesNameCn()));
            }
            eq(q, "material_type", bo.getMaterialType());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByDesc("update_time");
    }
}
