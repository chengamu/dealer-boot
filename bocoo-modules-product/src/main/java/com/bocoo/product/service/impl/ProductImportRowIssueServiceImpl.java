package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductImportRowIssueBo;
import com.bocoo.product.domain.entity.ProductImportRowIssue;
import com.bocoo.product.domain.vo.ProductImportRowIssueVo;
import com.bocoo.product.mapper.ProductImportRowIssueMapper;
import com.bocoo.product.service.ProductEntityDefaults;
import com.bocoo.product.service.ProductImportRowIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class ProductImportRowIssueServiceImpl implements ProductImportRowIssueService {

    private static final String ISSUE_STATUS_PENDING = "1";

    private final ProductImportRowIssueMapper importRowIssueMapper;

    @Override
    public TableDataInfo<ProductImportRowIssueVo> queryPageList(ProductImportRowIssueBo bo, PageQuery pageQuery) {
        Page<ProductImportRowIssueVo> result = importRowIssueMapper.selectVoPage(pageQuery.build(), buildQueryWrapper(bo));
        return TableDataInfo.build(result);
    }

    @Override
    public ProductImportRowIssueVo queryById(Long issueId) {
        return importRowIssueMapper.selectVoById(issueId);
    }

    @Override
    public Boolean save(ProductImportRowIssueBo bo) {
        ProductImportRowIssue entity = MapstructUtils.convert(bo, ProductImportRowIssue.class);
        if (entity == null) {
            return Boolean.FALSE;
        }
        if (StringUtils.isBlank(entity.getStatus())) {
            entity.setStatus(ISSUE_STATUS_PENDING);
        }
        if (entity.getIssueId() == null) {
            ProductEntityDefaults.prepareInsert(entity);
            return importRowIssueMapper.insert(entity) > 0;
        }
        return importRowIssueMapper.updateById(entity) > 0;
    }

    @Override
    public Boolean deleteByIds(Long[] ids) {
        return importRowIssueMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    private LambdaQueryWrapper<ProductImportRowIssue> buildQueryWrapper(ProductImportRowIssueBo bo) {
        if (bo == null) {
            bo = new ProductImportRowIssueBo();
        }
        LambdaQueryWrapper<ProductImportRowIssue> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getBatchId() != null, ProductImportRowIssue::getBatchId, bo.getBatchId());
        lqw.eq(bo.getRowNo() != null, ProductImportRowIssue::getRowNo, bo.getRowNo());
        lqw.like(StringUtils.isNotBlank(bo.getColumnName()), ProductImportRowIssue::getColumnName, bo.getColumnName());
        lqw.like(StringUtils.isNotBlank(bo.getIssueLevel()), ProductImportRowIssue::getIssueLevel, bo.getIssueLevel());
        lqw.like(StringUtils.isNotBlank(bo.getIssueCode()), ProductImportRowIssue::getIssueCode, bo.getIssueCode());
        lqw.like(StringUtils.isNotBlank(bo.getStatus()), ProductImportRowIssue::getStatus, bo.getStatus());
        lqw.orderByAsc(ProductImportRowIssue::getRowNo);
        return lqw;
    }
}
