package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.FabricProfileBo;
import com.bocoo.product.domain.bo.FabricSeriesBo;
import com.bocoo.product.domain.entity.FabricProfile;
import com.bocoo.product.domain.entity.FabricSeries;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.FabricProfileVo;
import com.bocoo.product.domain.vo.FabricSeriesVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.FabricProfileMapper;
import com.bocoo.product.mapper.FabricSeriesMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 面料系列和面料资料服务。
 */
@Service
@RequiredArgsConstructor
public class ProductFabricService extends ProductCrudSupport {

    private final FabricSeriesMapper fabricSeriesMapper;
    private final FabricProfileMapper fabricProfileMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    public TableDataInfo<FabricSeriesVo> queryFabricSeriesPage(FabricSeriesBo bo, PageQuery pageQuery) {
        return page(fabricSeriesMapper, pageQuery, fabricSeriesWrapper(bo));
    }

    public List<FabricSeriesVo> queryFabricSeriesList(FabricSeriesBo bo) {
        return fabricSeriesMapper.selectVoList(fabricSeriesWrapper(bo));
    }

    public FabricSeriesVo getFabricSeriesById(Long id) {
        return fabricSeriesMapper.selectVoById(id);
    }

    public Boolean saveFabricSeries(FabricSeriesBo bo) {
        return save(fabricSeriesMapper, bo, FabricSeries.class, "seriesId");
    }

    public Boolean removeFabricSeriesByIds(Long[] ids) {
        return remove(fabricSeriesMapper, ids);
    }

    public Boolean updateFabricSeriesStatus(Long id, String status) {
        return updateStatus(fabricSeriesMapper, FabricSeries.class, "seriesId", id, status);
    }

    public ReferenceCheckResultVo checkFabricSeriesReferences(Long seriesId) {
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

    public TableDataInfo<FabricProfileVo> queryFabricProfilePage(FabricProfileBo bo, PageQuery pageQuery) {
        return page(fabricProfileMapper, pageQuery, fabricProfileWrapper(bo));
    }

    public List<FabricProfileVo> queryFabricProfileList(FabricProfileBo bo) {
        return fabricProfileMapper.selectVoList(fabricProfileWrapper(bo));
    }

    public FabricProfileVo getFabricProfileById(Long id) {
        return fabricProfileMapper.selectVoById(id);
    }

    public Boolean saveFabricProfile(FabricProfileBo bo) {
        if (bo != null && StringUtils.isNotBlank(bo.getMaterialCode())) {
            bo.setFabricCode(bo.getMaterialCode());
        }
        return save(fabricProfileMapper, bo, FabricProfile.class, "fabricId");
    }

    public Boolean removeFabricProfileByIds(Long[] ids) {
        return remove(fabricProfileMapper, ids);
    }

    public Boolean updateFabricProfileStatus(Long id, String status) {
        return updateStatus(fabricProfileMapper, FabricProfile.class, "fabricId", id, status);
    }

    public ReferenceCheckResultVo checkFabricProfileReferences(Long profileId) {
        long bindingCount = mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "FABRIC_PROFILE").eq("target_id", profileId));
        ReferenceCheckResultVo result = referenceResult(bindingCount, "product.fabricProfile.hasReferences", null);
        if (bindingCount > 0) {
            result.getReferenceSummaries().add("Media bindings: " + bindingCount);
        }
        return result;
    }

    private QueryWrapper<FabricSeries> fabricSeriesWrapper(FabricSeriesBo bo) {
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

    private QueryWrapper<FabricProfile> fabricProfileWrapper(FabricProfileBo bo) {
        QueryWrapper<FabricProfile> q = activeQuery(FabricProfile.class);
        if (bo != null) {
            like(q, "material_code", bo.getMaterialCode());
            like(q, "material_name_cn", bo.getMaterialNameCn());
            eq(q, "series_id", bo.getSeriesId());
            like(q, "series_code", bo.getSeriesCode());
            like(q, "color_code", bo.getColorCode());
            like(q, "color_name", bo.getColorName());
            like(q, "sample_book_no", bo.getSampleBookNo());
            like(q, "vendor_item_no", bo.getVendorItemNo());
            like(q, "supplier_code", bo.getSupplierCode());
            eq(q, "status", bo.getStatus());
        }
        return q.orderByDesc("update_time");
    }
}
