package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.FabricProfileBo;
import com.bocoo.product.domain.entity.FabricProfile;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import com.bocoo.product.domain.vo.FabricProfileVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.mapper.FabricProfileMapper;
import com.bocoo.product.mapper.ProductMediaBindingMapper;
import com.bocoo.product.service.FabricProfileService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FabricProfileServiceImpl extends ProductServiceSupport implements FabricProfileService {

    private final FabricProfileMapper fabricProfileMapper;
    private final ProductMediaBindingMapper mediaBindingMapper;

    @Override
    public TableDataInfo<FabricProfileVo> queryPageList(FabricProfileBo bo, PageQuery pageQuery) {
        return page(fabricProfileMapper, pageQuery, buildQueryWrapper(bo));
    }

    @Override
    public List<FabricProfileVo> queryList(FabricProfileBo bo) {
        return fabricProfileMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public FabricProfileVo queryById(Long id) {
        return fabricProfileMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(FabricProfileBo bo) {
        normalizeFabricCode(bo);
        FabricProfile entity = MapstructUtils.convert(bo, FabricProfile.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return fabricProfileMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(FabricProfileBo bo) {
        normalizeFabricCode(bo);
        FabricProfile entity = MapstructUtils.convert(bo, FabricProfile.class);
        return entity != null && fabricProfileMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return remove(fabricProfileMapper, ids);
    }

    @Override
    public Boolean updateStatus(Long id, String status) {
        return fabricProfileMapper.update(null, new LambdaUpdateWrapper<FabricProfile>()
            .eq(FabricProfile::getFabricId, id)
            .set(FabricProfile::getStatus, status)) > 0;
    }

    @Override
    public ReferenceCheckResultVo checkReferences(Long profileId) {
        long bindingCount = mediaBindingMapper.selectCount(activeQuery(ProductMediaBinding.class).eq("target_type", "FABRIC_PROFILE").eq("target_id", profileId));
        ReferenceCheckResultVo result = referenceResult(bindingCount, "product.fabricProfile.hasReferences", null);
        if (bindingCount > 0) {
            result.getReferenceSummaries().add("Media bindings: " + bindingCount);
        }
        return result;
    }

    private QueryWrapper<FabricProfile> buildQueryWrapper(FabricProfileBo bo) {
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

    private void normalizeFabricCode(FabricProfileBo bo) {
        if (bo != null && StringUtils.isNotBlank(bo.getMaterialCode())) {
            bo.setFabricCode(bo.getMaterialCode());
        }
    }
}
