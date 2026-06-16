package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.FabricProfileBo;
import com.bocoo.product.domain.vo.FabricProfileVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface FabricProfileService {

    TableDataInfo<FabricProfileVo> queryPageList(FabricProfileBo bo, PageQuery pageQuery);

    List<FabricProfileVo> queryList(FabricProfileBo bo);

    FabricProfileVo queryById(Long id);

    Boolean insertByBo(FabricProfileBo bo);

    Boolean updateByBo(FabricProfileBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long profileId);
}
