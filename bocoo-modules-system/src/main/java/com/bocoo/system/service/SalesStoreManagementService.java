package com.bocoo.system.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.system.domain.bo.SalesStoreBo;
import com.bocoo.system.domain.vo.SalesStoreDeptOptionVo;
import com.bocoo.system.domain.vo.SalesStoreReferenceVo;
import com.bocoo.system.domain.vo.SalesStoreOptionVo;
import com.bocoo.system.domain.vo.SalesStoreVo;

import java.util.List;

public interface SalesStoreManagementService extends SalesStoreService {

    TableDataInfo<SalesStoreVo> queryPageList(SalesStoreBo bo, PageQuery pageQuery);

    List<SalesStoreOptionVo> queryOptions();

    List<SalesStoreDeptOptionVo> queryDeptOptions();

    SalesStoreVo queryManagementById(Long salesStoreId);

    boolean insertByBo(SalesStoreBo bo);

    boolean updateByBo(SalesStoreBo bo);

    int updateStatus(Long salesStoreId, String status);

    SalesStoreReferenceVo checkDisableReferences(Long salesStoreId);
}
