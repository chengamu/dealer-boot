package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PublishApprovalBo;
import com.bocoo.product.domain.bo.PublishCheckBo;
import com.bocoo.product.domain.vo.PublishApprovalVo;

import java.util.List;

public interface PublishApprovalService {

    TableDataInfo<PublishApprovalVo> queryPageList(PublishApprovalBo bo, PageQuery pageQuery);

    List<PublishApprovalVo> queryList(PublishApprovalBo bo);

    PublishApprovalVo queryById(Long id);

    Boolean save(PublishApprovalBo bo);

    Boolean deleteByIds(Long[] ids);

    Boolean updateApprovalStatus(Long approvalId, String approvalStatus, String approvalComment);

    PublishApprovalVo submit(PublishCheckBo bo);
}
