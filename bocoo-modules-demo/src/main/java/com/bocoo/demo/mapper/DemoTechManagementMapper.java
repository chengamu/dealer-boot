package com.bocoo.demo.mapper;

import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.demo.domain.entity.DemoTechManagement;
import com.bocoo.demo.domain.vo.DemoTechManagementVo;
import com.bocoo.demo.domain.vo.DemoTechnologyStatusCountVo;

import java.util.List;

/**
 * 技术管理示例数据层。
 */
public interface DemoTechManagementMapper extends BaseMapperPlus<DemoTechManagement, DemoTechManagementVo> {

    List<DemoTechnologyStatusCountVo> selectTechnologyStatusCount();
}
