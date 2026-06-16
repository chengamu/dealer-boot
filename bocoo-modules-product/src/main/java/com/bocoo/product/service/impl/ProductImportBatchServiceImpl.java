package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.TimeUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductImportBatchBo;
import com.bocoo.product.domain.entity.ProductImportBatch;
import com.bocoo.product.domain.entity.ProductImportRowIssue;
import com.bocoo.product.domain.vo.ProductImportBatchVo;
import com.bocoo.product.mapper.ProductImportBatchMapper;
import com.bocoo.product.mapper.ProductImportRowIssueMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductImportBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ProductImportBatchServiceImpl implements ProductImportBatchService {

    private static final String DEFAULT_SOURCE_SYSTEM = "MANUAL";
    private static final String DEFAULT_IMPORT_STATUS = "DRAFT";
    private static final String DEFAULT_EXCEL_IMPORT_TYPE = "MIXED_XLS";

    private final ProductImportBatchMapper importBatchMapper;
    private final ProductImportRowIssueMapper importRowIssueMapper;

    @Override
    public TableDataInfo<ProductImportBatchVo> queryPageList(ProductImportBatchBo bo, PageQuery pageQuery) {
        Page<ProductImportBatchVo> result = importBatchMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public ProductImportBatchVo queryById(Long batchId) {
        return importBatchMapper.selectVoById(batchId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean save(ProductImportBatchBo bo) {
        ProductImportBatch entity = MapstructUtils.convert(bo, ProductImportBatch.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        fillDefaults(entity);
        if (entity.getBatchId() == null) {
            entity.setBatchCode(StringUtils.blankToDefault(entity.getBatchCode(), buildBatchCode()));
            ProductEntityDefaults.prepareInsert(entity);
            return importBatchMapper.insert(entity) > 0;
        }
        return importBatchMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean updateStatus(Long batchId, String status) {
        ProductImportBatch entity = new ProductImportBatch();
        entity.setBatchId(batchId);
        entity.setImportStatus(status);
        if ("COMMITTED".equals(status) || "FAILED".equals(status) || "CANCELED".equals(status)) {
            entity.setFinishedTime(TimeUtils.utcNow());
        }
        return importBatchMapper.updateById(entity) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByIds(Long[] ids) {
        importRowIssueMapper.delete(Wrappers.lambdaQuery(ProductImportRowIssue.class)
            .in(ProductImportRowIssue::getBatchId, Arrays.asList(ids)));
        return importBatchMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<ProductImportBatch> buildQueryWrapper(ProductImportBatchBo bo) {
        if (bo == null) {
            bo = new ProductImportBatchBo();
        }
        LambdaQueryWrapper<ProductImportBatch> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getBatchCode()), ProductImportBatch::getBatchCode, bo.getBatchCode());
        lqw.like(StringUtils.isNotBlank(bo.getImportType()), ProductImportBatch::getImportType, bo.getImportType());
        lqw.like(StringUtils.isNotBlank(bo.getSourceSystem()), ProductImportBatch::getSourceSystem, bo.getSourceSystem());
        lqw.like(StringUtils.isNotBlank(bo.getSourceFileName()), ProductImportBatch::getSourceFileName, bo.getSourceFileName());
        lqw.like(StringUtils.isNotBlank(bo.getTargetObjectType()), ProductImportBatch::getTargetObjectType, bo.getTargetObjectType());
        lqw.like(StringUtils.isNotBlank(bo.getTargetObjectCode()), ProductImportBatch::getTargetObjectCode, bo.getTargetObjectCode());
        lqw.like(StringUtils.isNotBlank(bo.getImportStatus()), ProductImportBatch::getImportStatus, bo.getImportStatus());
        lqw.orderByDesc(ProductImportBatch::getCreateTime);
        return lqw;
    }

    private void fillDefaults(ProductImportBatch entity) {
        if (StringUtils.isBlank(entity.getSourceSystem())) {
            entity.setSourceSystem(DEFAULT_SOURCE_SYSTEM);
        }
        if (StringUtils.isBlank(entity.getImportType())) {
            entity.setImportType(DEFAULT_EXCEL_IMPORT_TYPE);
        }
        if (StringUtils.isBlank(entity.getImportStatus())) {
            entity.setImportStatus(DEFAULT_IMPORT_STATUS);
        }
        if (entity.getTotalRows() == null) {
            entity.setTotalRows(0);
        }
        if (entity.getSuccessRows() == null) {
            entity.setSuccessRows(0);
        }
        if (entity.getWarningRows() == null) {
            entity.setWarningRows(0);
        }
        if (entity.getFailedRows() == null) {
            entity.setFailedRows(0);
        }
        if (entity.getStartedTime() == null) {
            entity.setStartedTime(TimeUtils.utcNow());
        }
    }

    private String buildBatchCode() {
        return "IMP-" + TimeUtils.utcNow().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }
}
