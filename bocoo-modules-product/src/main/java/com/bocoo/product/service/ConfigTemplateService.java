package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigTemplateBo;
import com.bocoo.product.domain.vo.ConfigTemplateVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ConfigTemplateService {

    TableDataInfo<ConfigTemplateVo> queryPageList(ConfigTemplateBo bo, PageQuery pageQuery);

    List<ConfigTemplateVo> queryList(ConfigTemplateBo bo);

    ConfigTemplateVo queryById(Long id);

    Boolean insertByBo(ConfigTemplateBo bo);

    Boolean updateByBo(ConfigTemplateBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
