package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ConfigOptionBo;
import com.bocoo.product.domain.vo.ConfigOptionVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface ConfigOptionService {

    TableDataInfo<ConfigOptionVo> queryPageList(ConfigOptionBo bo, PageQuery pageQuery);

    List<ConfigOptionVo> queryList(ConfigOptionBo bo);

    ConfigOptionVo queryById(Long id);

    Boolean insertByBo(ConfigOptionBo bo);

    Boolean updateByBo(ConfigOptionBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long id);
}
