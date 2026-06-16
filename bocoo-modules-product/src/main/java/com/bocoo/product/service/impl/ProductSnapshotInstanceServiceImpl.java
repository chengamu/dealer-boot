package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.entity.ProductSnapshotInstance;
import com.bocoo.product.domain.vo.ProductSnapshotInstanceVo;
import com.bocoo.product.mapper.ProductSnapshotInstanceMapper;
import com.bocoo.product.service.ProductSnapshotInstanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductSnapshotInstanceServiceImpl implements ProductSnapshotInstanceService {

    private final ProductSnapshotInstanceMapper snapshotInstanceMapper;

    @Override
    public TableDataInfo<ProductSnapshotInstanceVo> queryPageList(ProductSnapshotInstance query, PageQuery pageQuery) {
        Page<ProductSnapshotInstanceVo> result = snapshotInstanceMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(query));
        return TableDataInfo.build(result);
    }

    @Override
    public ProductSnapshotInstanceVo queryById(Long snapshotId) {
        return snapshotInstanceMapper.selectVoById(snapshotId);
    }

    private LambdaQueryWrapper<ProductSnapshotInstance> buildQueryWrapper(ProductSnapshotInstance query) {
        if (query == null) {
            query = new ProductSnapshotInstance();
        }
        LambdaQueryWrapper<ProductSnapshotInstance> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotBlank(query.getSourceSystem()), ProductSnapshotInstance::getSourceSystem, query.getSourceSystem());
        lqw.like(StringUtils.isNotBlank(query.getSourceBizType()), ProductSnapshotInstance::getSourceBizType, query.getSourceBizType());
        lqw.like(StringUtils.isNotBlank(query.getSourceBizNo()), ProductSnapshotInstance::getSourceBizNo, query.getSourceBizNo());
        lqw.like(StringUtils.isNotBlank(query.getSourceBizLineNo()), ProductSnapshotInstance::getSourceBizLineNo, query.getSourceBizLineNo());
        lqw.like(StringUtils.isNotBlank(query.getCustomerCode()), ProductSnapshotInstance::getCustomerCode, query.getCustomerCode());
        lqw.like(StringUtils.isNotBlank(query.getPackageCode()), ProductSnapshotInstance::getPackageCode, query.getPackageCode());
        lqw.like(StringUtils.isNotBlank(query.getSnapshotHash()), ProductSnapshotInstance::getSnapshotHash, query.getSnapshotHash());
        lqw.like(StringUtils.isNotBlank(query.getSnapshotStatus()), ProductSnapshotInstance::getSnapshotStatus, query.getSnapshotStatus());
        lqw.orderByDesc(ProductSnapshotInstance::getBuiltTime);
        return lqw;
    }
}
