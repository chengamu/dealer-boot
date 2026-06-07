package com.bocoo.product.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.*;
import com.bocoo.product.domain.entity.*;
import com.bocoo.product.domain.vo.*;
import com.bocoo.product.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 产品价格中心服务。
 */
@Service
@RequiredArgsConstructor
public class ProductPricingService {

    private final PricePlanMapper pricePlanIdMapper;
    private final PricePlanVersionMapper pricePlanVersionIdMapper;
    private final PriceRuleItemMapper ruleItemIdMapper;
    private final PriceCalculationEngine priceCalculationEngine;

    public TableDataInfo<PricePlanVo> queryPricePlanPage(PricePlanBo bo, PageQuery pageQuery) {
        Page<PricePlanVo> result = pricePlanIdMapper.selectVoPage(pageQuery.build(), buildPricePlanWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<PricePlanVo> queryPricePlanList(PricePlanBo bo) {
        return pricePlanIdMapper.selectVoList(buildPricePlanWrapper(bo));
    }

    public PricePlanVo getPricePlanById(Long id) {
        return pricePlanIdMapper.selectVoById(id);
    }

    public Boolean savePricePlan(PricePlanBo bo) {
        PricePlan entity = MapstructUtils.convert(bo, PricePlan.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getPricePlanId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return pricePlanIdMapper.insert(entity) > 0;
        }
        return pricePlanIdMapper.updateById(entity) > 0;
    }

    public Boolean removePricePlanByIds(Long[] ids) {
        return pricePlanIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<PricePlan> buildPricePlanWrapper(PricePlanBo bo) {
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


    public TableDataInfo<PricePlanVersionVo> queryPricePlanVersionPage(PricePlanVersionBo bo, PageQuery pageQuery) {
        Page<PricePlanVersionVo> result = pricePlanVersionIdMapper.selectVoPage(pageQuery.build(), buildPricePlanVersionWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<PricePlanVersionVo> queryPricePlanVersionList(PricePlanVersionBo bo) {
        return pricePlanVersionIdMapper.selectVoList(buildPricePlanVersionWrapper(bo));
    }

    public PricePlanVersionVo getPricePlanVersionById(Long id) {
        return pricePlanVersionIdMapper.selectVoById(id);
    }

    public Boolean savePricePlanVersion(PricePlanVersionBo bo) {
        PricePlanVersion entity = MapstructUtils.convert(bo, PricePlanVersion.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getPricePlanVersionId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return pricePlanVersionIdMapper.insert(entity) > 0;
        }
        return pricePlanVersionIdMapper.updateById(entity) > 0;
    }

    public Boolean removePricePlanVersionByIds(Long[] ids) {
        return pricePlanVersionIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<PricePlanVersion> buildPricePlanVersionWrapper(PricePlanVersionBo bo) {
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


    public TableDataInfo<PriceRuleItemVo> queryPriceRuleItemPage(PriceRuleItemBo bo, PageQuery pageQuery) {
        Page<PriceRuleItemVo> result = ruleItemIdMapper.selectVoPage(pageQuery.build(), buildPriceRuleItemWrapper(bo));
        return TableDataInfo.build(result);
    }

    public java.util.List<PriceRuleItemVo> queryPriceRuleItemList(PriceRuleItemBo bo) {
        return ruleItemIdMapper.selectVoList(buildPriceRuleItemWrapper(bo));
    }

    public PriceRuleItemVo getPriceRuleItemById(Long id) {
        return ruleItemIdMapper.selectVoById(id);
    }

    public Boolean savePriceRuleItem(PriceRuleItemBo bo) {
        PriceRuleItem entity = MapstructUtils.convert(bo, PriceRuleItem.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getRuleItemId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return ruleItemIdMapper.insert(entity) > 0;
        }
        return ruleItemIdMapper.updateById(entity) > 0;
    }

    public Boolean removePriceRuleItemByIds(Long[] ids) {
        return ruleItemIdMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<PriceRuleItem> buildPriceRuleItemWrapper(PriceRuleItemBo bo) {
        if (bo == null) {
            bo = new PriceRuleItemBo();
        }
        LambdaQueryWrapper<PriceRuleItem> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getPricePlanVersionId() != null, PriceRuleItem::getPricePlanVersionId, bo.getPricePlanVersionId());
        lqw.like(StringUtils.isNotBlank(bo.getPricePlanCode()), PriceRuleItem::getPricePlanCode, bo.getPricePlanCode());
        lqw.like(StringUtils.isNotBlank(bo.getItemCode()), PriceRuleItem::getItemCode, bo.getItemCode());
        lqw.like(StringUtils.isNotBlank(bo.getItemNameCn()), PriceRuleItem::getItemNameCn, bo.getItemNameCn());
        lqw.like(StringUtils.isNotBlank(bo.getItemNameEn()), PriceRuleItem::getItemNameEn, bo.getItemNameEn());
        lqw.like(StringUtils.isNotBlank(bo.getItemType()), PriceRuleItem::getItemType, bo.getItemType());
        lqw.like(StringUtils.isNotBlank(bo.getCurrencyCode()), PriceRuleItem::getCurrencyCode, bo.getCurrencyCode());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), PriceRuleItem::getStatus, bo.getStatus());
        return lqw;
    }

    public PriceCalculationResultVo calculate(PriceCalculationBo bo) {
        if (bo == null) {
            bo = new PriceCalculationBo();
        }
        PriceRuleItemBo ruleBo = new PriceRuleItemBo();
        ruleBo.setPricePlanVersionId(bo.getPricePlanVersionId());
        ruleBo.setCurrencyCode(bo.getCurrencyCode());
        ruleBo.setStatus("1");
        return priceCalculationEngine.calculate(bo, queryPriceRuleItemList(ruleBo));
    }
}
