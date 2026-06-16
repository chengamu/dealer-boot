package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.product.domain.bo.PublishApprovalBo;
import com.bocoo.product.domain.bo.PublishCheckBo;
import com.bocoo.product.domain.entity.PublishApproval;
import com.bocoo.product.domain.vo.PublishApprovalVo;
import com.bocoo.product.domain.vo.PublishCheckSummaryVo;
import com.bocoo.product.mapper.PublishApprovalMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductPublishWorkflowService;
import com.bocoo.product.service.PublishApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PublishApprovalServiceImpl implements PublishApprovalService {

    private final PublishApprovalMapper approvalMapper;
    private final ProductPublishWorkflowService publishWorkflowService;

    @Override
    public TableDataInfo<PublishApprovalVo> queryPageList(PublishApprovalBo bo, PageQuery pageQuery) {
        Page<PublishApprovalVo> result = approvalMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public List<PublishApprovalVo> queryList(PublishApprovalBo bo) {
        return approvalMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public PublishApprovalVo queryById(Long id) {
        return approvalMapper.selectVoById(id);
    }

    @Override
    public Boolean save(PublishApprovalBo bo) {
        PublishApproval entity = MapstructUtils.convert(bo, PublishApproval.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (entity.getApprovalId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return approvalMapper.insert(entity) > 0;
        }
        return approvalMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return approvalMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    @Override
    public Boolean updateApprovalStatus(Long approvalId, String approvalStatus, String approvalComment) {
        PublishApproval current = approvalMapper.selectById(approvalId);
        if (current == null) {
            throw ServiceException.ofMessageKey("product.publish.approval.notFound");
        }
        if (!"SUBMITTED".equals(current.getApprovalStatus())) {
            throw ServiceException.ofMessageKey("product.publish.approval.submittedOnly");
        }
        PublishApproval entity = new PublishApproval();
        entity.setApprovalId(approvalId);
        entity.setApprovalStatus(approvalStatus);
        entity.setApproverUserId(LoginHelper.getUserId());
        entity.setApproverName(LoginHelper.getUsername());
        entity.setApprovedTime(TimeUtils.utcNow());
        entity.setApprovalComment(approvalComment);
        entity.setStatus("1");
        return approvalMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PublishApprovalVo submit(PublishCheckBo bo) {
        PublishCheckSummaryVo summary = publishWorkflowService.check(bo);
        if (summary.getBlockerCount() > 0) {
            throw ServiceException.ofMessageKey("product.publish.approval.blocker");
        }
        String snapshot = PublishSnapshotSupport.snapshotJson(bo);
        String hash = PublishSnapshotSupport.sha256(snapshot);
        PublishApproval existing = approvalMapper.selectOne(Wrappers.<PublishApproval>lambdaQuery()
            .eq(PublishApproval::getSnapshotHash, hash)
            .eq(PublishApproval::getStatus, "1")
            .in(PublishApproval::getApprovalStatus, List.of("SUBMITTED", "APPROVED"))
            .orderByDesc(PublishApproval::getCreateTime)
            .last("limit 1"));
        if (existing != null) {
            return approvalMapper.selectVoById(existing.getApprovalId());
        }
        PublishApproval entity = new PublishApproval();
        entity.setTargetType("PRODUCT");
        entity.setTargetId(bo.getProductModelId());
        entity.setTargetCode(StringUtils.blankToDefault(bo.getProductModelCode(), bo.getSalesVariantCode()));
        entity.setApprovalStatus("SUBMITTED");
        entity.setSubmitterUserId(LoginHelper.getUserId());
        entity.setSubmitterName(LoginHelper.getUsername());
        entity.setSnapshotHash(hash);
        entity.setSnapshotJson(snapshot);
        entity.setStatus("1");
        ProductEntityDefaults.prepareInsert(entity);
        approvalMapper.insert(entity);
        return approvalMapper.selectVoById(entity.getApprovalId());
    }

    private LambdaQueryWrapper<PublishApproval> buildQueryWrapper(PublishApprovalBo bo) {
        if (bo == null) {
            bo = new PublishApprovalBo();
        }
        LambdaQueryWrapper<PublishApproval> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTargetType()), PublishApproval::getTargetType, bo.getTargetType());
        lqw.like(StringUtils.isNotBlank(bo.getTargetCode()), PublishApproval::getTargetCode, bo.getTargetCode());
        lqw.like(StringUtils.isNotBlank(bo.getApprovalStatus()), PublishApproval::getApprovalStatus, bo.getApprovalStatus());
        lqw.like(StringUtils.isNotBlank(bo.getSubmitterName()), PublishApproval::getSubmitterName, bo.getSubmitterName());
        lqw.like(StringUtils.isNotBlank(bo.getApproverName()), PublishApproval::getApproverName, bo.getApproverName());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), PublishApproval::getStatus, bo.getStatus());
        return lqw;
    }
}
