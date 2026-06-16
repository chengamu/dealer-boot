package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.QuestionGroupBo;
import com.bocoo.product.domain.vo.QuestionGroupVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface QuestionGroupService {

    TableDataInfo<QuestionGroupVo> queryPageList(QuestionGroupBo bo, PageQuery pageQuery);

    List<QuestionGroupVo> queryList(QuestionGroupBo bo);

    QuestionGroupVo queryById(Long id);

    Boolean insertByBo(QuestionGroupBo bo);

    Boolean updateByBo(QuestionGroupBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
