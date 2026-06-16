package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigQuestionBo;
import com.bocoo.product.domain.vo.ConfigQuestionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ConfigQuestionService {

    TableDataInfo<ConfigQuestionVo> queryPageList(ConfigQuestionBo bo, PageQuery pageQuery);

    List<ConfigQuestionVo> queryList(ConfigQuestionBo bo);

    ConfigQuestionVo queryById(Long id);

    Boolean insertByBo(ConfigQuestionBo bo);

    Boolean updateByBo(ConfigQuestionBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
