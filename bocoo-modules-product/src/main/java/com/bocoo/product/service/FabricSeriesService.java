package com.bocoo.product.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.FabricSeriesBo;
import com.bocoo.product.domain.vo.FabricSeriesVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;

import java.util.List;

public interface FabricSeriesService {

    TableDataInfo<FabricSeriesVo> queryPageList(FabricSeriesBo bo, PageQuery pageQuery);

    List<FabricSeriesVo> queryList(FabricSeriesBo bo);

    FabricSeriesVo queryById(Long id);

    Boolean insertByBo(FabricSeriesBo bo);

    Boolean updateByBo(FabricSeriesBo bo);

    Boolean deleteWithValidByIds(Long[] ids);

    Boolean updateStatus(Long id, String status);

    ReferenceCheckResultVo checkReferences(Long seriesId);
}
