package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductSyncOutboxBo;
import com.bocoo.product.domain.entity.ProductSyncOutbox;
import com.bocoo.product.domain.vo.ProductSyncOutboxVo;
import com.bocoo.product.mapper.ProductSyncOutboxMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductSyncOutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSyncOutboxServiceImpl implements ProductSyncOutboxService {

    private final ProductSyncOutboxMapper outboxMapper;

    @Override
    public TableDataInfo<ProductSyncOutboxVo> queryPageList(ProductSyncOutboxBo bo, PageQuery pageQuery) {
        Page<ProductSyncOutboxVo> result = outboxMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<ProductSyncOutboxVo> queryList(ProductSyncOutboxBo bo) {
        return outboxMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public ProductSyncOutboxVo queryById(Long id) {
        return outboxMapper.selectVoById(id);
    }

    @Override
    public Boolean save(ProductSyncOutboxBo bo) {
        ProductSyncOutbox entity = MapstructUtils.convert(bo, ProductSyncOutbox.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getOutboxId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return outboxMapper.insert(entity) > 0;
        }
        return outboxMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return outboxMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    @Override
    public Boolean retry(Long outboxId) {
        ProductSyncOutbox current = outboxMapper.selectById(outboxId);
        if (current == null) {
            return Boolean.FALSE;
        }
        ProductSyncOutbox entity = new ProductSyncOutbox();
        entity.setOutboxId(outboxId);
        entity.setSyncStatus("PENDING");
        entity.setRetryCount((current.getRetryCount() == null ? 0 : current.getRetryCount()) + 1);
        entity.setNextRetryTime(TimeUtils.utcNow());
        entity.setLastErrorKey(null);
        entity.setLastErrorMessage(null);
        return outboxMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<ProductSyncOutbox> buildQueryWrapper(ProductSyncOutboxBo bo) {
        if (bo == null) {
            bo = new ProductSyncOutboxBo();
        }
        LambdaQueryWrapper<ProductSyncOutbox> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getPackageCode()), ProductSyncOutbox::getPackageCode, bo.getPackageCode());
        lqw.like(StringUtils.isNotBlank(bo.getTargetSystem()), ProductSyncOutbox::getTargetSystem, bo.getTargetSystem());
        lqw.like(StringUtils.isNotBlank(bo.getEventType()), ProductSyncOutbox::getEventType, bo.getEventType());
        lqw.like(StringUtils.isNotBlank(bo.getSyncStatus()), ProductSyncOutbox::getSyncStatus, bo.getSyncStatus());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductSyncOutbox::getStatus, bo.getStatus());
        lqw.orderByDesc(ProductSyncOutbox::getCreateTime);
        return lqw;
    }
}
