package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PricePlanBo;
import com.bocoo.product.domain.entity.PricePlan;
import com.bocoo.product.domain.vo.PricePlanVo;
import com.bocoo.product.mapper.PricePlanMapper;
import com.bocoo.product.service.PricePlanService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricePlanServiceImpl implements PricePlanService {

    private final PricePlanMapper pricePlanMapper;

    @Override
    public TableDataInfo<PricePlanVo> queryPageList(PricePlanBo bo, PageQuery pageQuery) {
        Page<PricePlanVo> result = pricePlanMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<PricePlanVo> queryList(PricePlanBo bo) {
        return pricePlanMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PricePlanVo queryById(Long id) {
        return pricePlanMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(PricePlanBo bo) {
        PricePlan entity = MapstructUtils.convert(bo, PricePlan.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return pricePlanMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(PricePlanBo bo) {
        PricePlan entity = MapstructUtils.convert(bo, PricePlan.class);
        return entity != null && pricePlanMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return pricePlanMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<PricePlan> buildQueryWrapper(PricePlanBo bo) {
        if (bo == null) {
            bo = new PricePlanBo();
        }
        LambdaQueryWrapper<PricePlan> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getPricePlanCode()), PricePlan::getPricePlanCode, bo.getPricePlanCode());
        lqw.like(StringUtils.isNotBlank(bo.getPricePlanNameCn()), PricePlan::getPricePlanNameCn, bo.getPricePlanNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getPricePlanNameEn()), PricePlan::getPricePlanNameEn, bo.getPricePlanNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getProductModelCode()), PricePlan::getProductModelCode, bo.getProductModelCode());
        lqw.like(StringUtils.isNotBlank(bo.getSalesVariantCode()), PricePlan::getSalesVariantCode, bo.getSalesVariantCode());
        lqw.like(StringUtils.isNotBlank(bo.getCurrencyCode()), PricePlan::getCurrencyCode, bo.getCurrencyCode());
        lqw.like(StringUtils.isNotBlank(bo.getPricingMode()), PricePlan::getPricingMode, bo.getPricingMode());
        lqw.like(StringUtils.isNotBlank(bo.getBizStatus()), PricePlan::getBizStatus, bo.getBizStatus());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), PricePlan::getStatus, bo.getStatus());
        lqw.eq(StringUtils.isBlank(bo.getDelFlag()), PricePlan::getDelFlag, "0");
        return lqw;
    }
}
