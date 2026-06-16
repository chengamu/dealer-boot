package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.PublishCheckResultBo;
import com.bocoo.product.domain.vo.PublishCheckResultVo;

import java.util.List;

public interface PublishCheckResultService {

    TableDataInfo<PublishCheckResultVo> queryPageList(PublishCheckResultBo bo, PageQuery pageQuery);

    TableDataInfo<PublishCheckResultVo> queryGapTaskPage(PublishCheckResultBo bo, PageQuery pageQuery);

    List<PublishCheckResultVo> queryList(PublishCheckResultBo bo);

    PublishCheckResultVo queryById(Long id);

    Boolean save(PublishCheckResultBo bo);

    Boolean deleteByIds(Long[] ids);

    Boolean resolveGapTask(Long checkId);
}
