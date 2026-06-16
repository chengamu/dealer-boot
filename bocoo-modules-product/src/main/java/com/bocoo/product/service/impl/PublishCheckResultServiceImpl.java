package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PublishCheckResultBo;
import com.bocoo.product.domain.entity.PublishCheckResult;
import com.bocoo.product.domain.vo.PublishCheckResultVo;
import com.bocoo.product.mapper.PublishCheckResultMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.PublishCheckResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublishCheckResultServiceImpl implements PublishCheckResultService {

    private final PublishCheckResultMapper checkResultMapper;

    @Override
    public TableDataInfo<PublishCheckResultVo> queryPageList(PublishCheckResultBo bo, PageQuery pageQuery) {
        Page<PublishCheckResultVo> result = checkResultMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public TableDataInfo<PublishCheckResultVo> queryGapTaskPage(PublishCheckResultBo bo, PageQuery pageQuery) {
        Page<PublishCheckResultVo> result = checkResultMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo)
            .in(PublishCheckResult::getCheckLevel, List.of("BLOCKER", "WARNING"))
            .eq(PublishCheckResult::getResolvedFlag, "0"));
        return TableDataInfo.build(result);
    }

    @Override
    public List<PublishCheckResultVo> queryList(PublishCheckResultBo bo) {
        return checkResultMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PublishCheckResultVo queryById(Long id) {
        return checkResultMapper.selectVoById(id);
    }

    @Override
    public Boolean save(PublishCheckResultBo bo) {
        PublishCheckResult entity = MapstructUtils.convert(bo, PublishCheckResult.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getCheckId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return checkResultMapper.insert(entity) > 0;
        }
        return checkResultMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return checkResultMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    @Override
    public Boolean resolveGapTask(Long checkId) {
        PublishCheckResult entity = new PublishCheckResult();
        entity.setCheckId(checkId);
        entity.setResolvedFlag("1");
        entity.setCheckStatus("RESOLVED");
        return checkResultMapper.updateById(entity) > 0;
    }

    private LambdaQueryWrapper<PublishCheckResult> buildQueryWrapper(PublishCheckResultBo bo) {
        if (bo == null) {
            bo = new PublishCheckResultBo();
        }
        LambdaQueryWrapper<PublishCheckResult> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTargetType()), PublishCheckResult::getTargetType, bo.getTargetType());
        lqw.like(StringUtils.isNotBlank(bo.getTargetCode()), PublishCheckResult::getTargetCode, bo.getTargetCode());
        lqw.like(StringUtils.isNotBlank(bo.getCheckCode()), PublishCheckResult::getCheckCode, bo.getCheckCode());
        lqw.like(StringUtils.isNotBlank(bo.getCheckLevel()), PublishCheckResult::getCheckLevel, bo.getCheckLevel());
        lqw.like(StringUtils.isNotBlank(bo.getCheckStatus()), PublishCheckResult::getCheckStatus, bo.getCheckStatus());
        lqw.like(StringUtils.isNotBlank(bo.getResolvedFlag()), PublishCheckResult::getResolvedFlag, bo.getResolvedFlag());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), PublishCheckResult::getStatus, bo.getStatus());
        return lqw;
    }
}
