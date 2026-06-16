package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PricePlanVersionBo;
import com.bocoo.product.domain.entity.PricePlanVersion;
import com.bocoo.product.domain.vo.PricePlanVersionVo;
import com.bocoo.product.mapper.PricePlanVersionMapper;
import com.bocoo.product.service.PricePlanVersionService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricePlanVersionServiceImpl implements PricePlanVersionService {

    private final PricePlanVersionMapper pricePlanVersionMapper;

    @Override
    public TableDataInfo<PricePlanVersionVo> queryPageList(PricePlanVersionBo bo, PageQuery pageQuery) {
        Page<PricePlanVersionVo> result = pricePlanVersionMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<PricePlanVersionVo> queryList(PricePlanVersionBo bo) {
        return pricePlanVersionMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PricePlanVersionVo queryById(Long id) {
        return pricePlanVersionMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(PricePlanVersionBo bo) {
        PricePlanVersion entity = MapstructUtils.convert(bo, PricePlanVersion.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return pricePlanVersionMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(PricePlanVersionBo bo) {
        PricePlanVersion entity = MapstructUtils.convert(bo, PricePlanVersion.class);
        return entity != null && pricePlanVersionMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return pricePlanVersionMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<PricePlanVersion> buildQueryWrapper(PricePlanVersionBo bo) {
        if (bo == null) {
            bo = new PricePlanVersionBo();
        }
        LambdaQueryWrapper<PricePlanVersion> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getPricePlanCode()), PricePlanVersion::getPricePlanCode, bo.getPricePlanCode());
        lqw.like(StringUtils.isNotBlank(bo.getVersionNo()), PricePlanVersion::getVersionNo, bo.getVersionNo());
        lqw.like(StringUtils.isNotBlank(bo.getVersionStatus()), PricePlanVersion::getVersionStatus, bo.getVersionStatus());
        lqw.like(StringUtils.isNotBlank(bo.getProductModelCode()), PricePlanVersion::getProductModelCode, bo.getProductModelCode());
        lqw.like(StringUtils.isNotBlank(bo.getSalesVariantCode()), PricePlanVersion::getSalesVariantCode, bo.getSalesVariantCode());
        lqw.like(StringUtils.isNotBlank(bo.getCurrencyCode()), PricePlanVersion::getCurrencyCode, bo.getCurrencyCode());
        lqw.like(StringUtils.isNotBlank(bo.getPricingMode()), PricePlanVersion::getPricingMode, bo.getPricingMode());
        return lqw;
    }
}
