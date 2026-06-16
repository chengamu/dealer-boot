package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigTemplateVersionBo;
import com.bocoo.product.domain.vo.ConfigTemplateVersionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ConfigTemplateVersionService {

    TableDataInfo<ConfigTemplateVersionVo> queryPageList(ConfigTemplateVersionBo bo, PageQuery pageQuery);

    List<ConfigTemplateVersionVo> queryList(ConfigTemplateVersionBo bo);

    ConfigTemplateVersionVo queryById(Long id);

    Boolean insertByBo(ConfigTemplateVersionBo bo);

    Boolean updateByBo(ConfigTemplateVersionBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
