package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.FabricSeriesBo;
import com.bocoo.product.domain.entity.FabricSeries;
import com.bocoo.product.domain.entity.ProductMaterial;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.FabricSeriesVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.FabricSeriesMapper;
import com.bocoo.product.mapper.ProductMaterialMapper;
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
    private final ProductMaterialMapper materialMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    @Override
    public TableDataInfo<FabricSeriesVo> queryPageList(FabricSeriesBo bo, PageQuery pageQuery) {
        return page(fabricSeriesMapper, pageQuery, buildQueryWrapper(bo), q -> q.orderByDesc("update_time"));
    }

    @Override
    public List<FabricSeriesVo> queryList(FabricSeriesBo bo) {
        return fabricSeriesMapper.selectVoList(applyDefaultSort(null, buildQueryWrapper(bo), q -> q.orderByDesc("update_time")));
    }

    @Override
    public FabricSeriesVo queryById(Long id) {
        return fabricSeriesMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(FabricSeriesBo bo) {
        validateSeriesCodeUnique(bo);
        FabricSeries entity = MapstructUtils.convert(bo, FabricSeries.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return fabricSeriesMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(FabricSeriesBo bo) {
        validateSeriesCodeUnique(bo);
        if (bo != null && bo.getSeriesId() != null) {
            FabricSeries current = fabricSeriesMapper.selectById(bo.getSeriesId());
            if (current != null) {
                assertNormalEditable(current.getStatus());
            }
        }
        FabricSeries entity = MapstructUtils.convert(bo, FabricSeries.class);
        return entity != null && fabricSeriesMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        for (Long id : ids) {
            assertNoReferences(checkReferences(id));
        }
        return remove(fabricSeriesMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return fabricSeriesMapper.update(null, new LambdaUpdateWrapper<FabricSeries>()
            .eq(FabricSeries::getSeriesId, id)
            .set(FabricSeries::getStatus, status)) > 0;
    }

    @Override
    public BaseEditCheckResultVo checkEditAllowed(Long id) {
        FabricSeries series = fabricSeriesMapper.selectById(id);
        if (series == null) {
            return deniedEditCheck(null, "product.base.edit.notFound", null);
        }
        return editCheckResult(series.getStatus(), checkReferences(id));
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long seriesId) {
        long materialCount = materialMapper.selectCount(activeQuery(ProductMaterial.class).eq("fabric_series_id", seriesId));
        long bindingCount = mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "FABRIC_SERIES").eq("target_id", seriesId));
        ReferenceCheckResultVo result = referenceResult(materialCount + bindingCount, "product.fabricSeries.hasReferences", null);
        if (materialCount > 0) {
            result.getReferenceSummaries().add("Fabric materials: " + materialCount);
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
        return q;
    }

    private void validateSeriesCodeUnique(FabricSeriesBo bo) {
        if (bo == null || StringUtils.isBlank(bo.getSeriesCode())) {
            return;
        }
        long count = fabricSeriesMapper.selectCount(activeQuery(FabricSeries.class)
            .eq("series_code", bo.getSeriesCode())
            .ne(bo.getSeriesId() != null, "series_id", bo.getSeriesId()));
        if (count > 0) {
            throw ServiceException.ofMessageKey("product.fabricSeries.codeExists");
        }
    }
}
