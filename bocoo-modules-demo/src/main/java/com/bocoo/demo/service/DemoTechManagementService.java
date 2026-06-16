package com.bocoo.demo.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.IBaseService;
import com.bocoo.demo.domain.bo.DemoTechManagementBo;
import com.bocoo.demo.domain.entity.DemoTechManagement;
import com.bocoo.demo.domain.vo.DemoTechManagementVo;
import com.bocoo.demo.domain.vo.DemoTechnologyStatusCountVo;

import java.util.Collection;
import java.util.List;

/**
 * 技术管理示例服务接口。
 */
public interface DemoTechManagementService extends IBaseService<DemoTechManagement, DemoTechManagementVo> {

    TableDataInfo<DemoTechManagementVo> queryPageList(DemoTechManagementBo bo, PageQuery pageQuery);

    List<DemoTechManagementVo> queryList(DemoTechManagementBo bo);

    DemoTechManagementVo queryById(Long managementId);

    Boolean insertByBo(DemoTechManagementBo bo);

    Boolean updateByBo(DemoTechManagementBo bo);

    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    int updateStatus(Long managementId, String status);

    List<DemoTechnologyStatusCountVo> selectTechnologyStatusCount();
}
