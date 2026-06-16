package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PriceRuleItemBo;
import com.bocoo.product.domain.entity.PriceRuleItem;
import com.bocoo.product.domain.vo.PriceRuleItemVo;
import com.bocoo.product.mapper.PriceRuleItemMapper;
import com.bocoo.product.service.PriceRuleItemService;
import com.bocoo.product.service.ProductEntityDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PriceRuleItemServiceImpl implements PriceRuleItemService {

    private final PriceRuleItemMapper ruleItemMapper;

    @Override
    public TableDataInfo<PriceRuleItemVo> queryPageList(PriceRuleItemBo bo, PageQuery pageQuery) {
        Page<PriceRuleItemVo> result = ruleItemMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<PriceRuleItemVo> queryList(PriceRuleItemBo bo) {
        return ruleItemMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PriceRuleItemVo queryById(Long id) {
        return ruleItemMapper.selectVoById(id);
    }

    @Override
    public Boolean insertByBo(PriceRuleItemBo bo) {
        PriceRuleItem entity = MapstructUtils.convert(bo, PriceRuleItem.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        ProductEntityDefaults.prepareInsert(entity);
        return ruleItemMapper.insert(entity) > 0;
    }

    @Override
    public Boolean updateByBo(PriceRuleItemBo bo) {
        PriceRuleItem entity = MapstructUtils.convert(bo, PriceRuleItem.class);
        return entity != null && ruleItemMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteWithValidByIds(Long[] ids) {
        return ruleItemMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<PriceRuleItem> buildQueryWrapper(PriceRuleItemBo bo) {
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
}
