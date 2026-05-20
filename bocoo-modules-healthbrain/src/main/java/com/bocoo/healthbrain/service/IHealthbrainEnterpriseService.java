package com.bocoo.healthbrain.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.IBaseService;
import com.bocoo.healthbrain.domain.bo.HealthbrainEnterpriseBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainEnterprise;
import com.bocoo.healthbrain.domain.vo.HealthbrainEnterpriseVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainEnterpriseTypeCountVo;

import java.util.Collection;
import java.util.List;

/**
 * 企业管理 服务接口
 *
 * @author cmx
 */
public interface IHealthbrainEnterpriseService extends IBaseService<HealthbrainEnterprise, HealthbrainEnterpriseVo> {

    /**
     * 查询企业管理列表（分页）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页数据
     */
    TableDataInfo<HealthbrainEnterpriseVo> queryPageList(HealthbrainEnterpriseBo bo, PageQuery pageQuery);

    /**
     * 查询企业管理列表
     *
     * @param bo 查询条件
     * @return 企业管理列表
     */
    List<HealthbrainEnterpriseVo> queryList(HealthbrainEnterpriseBo bo);

    /**
     * 根据业务对象新增
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean insertByBo(HealthbrainEnterpriseBo bo);

    /**
     * 根据业务对象修改
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean updateByBo(HealthbrainEnterpriseBo bo);

    /**
     * 批量删除并校验
     * @param ids 主键集合
     * @param isValid 是否校验
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

     /**
     * 修改设备分组状态
     *
     * @param id 设备分组主键
     * @param status 状态值
     * @return 更新记录数
     */
    int updateStatus(Long id, String status);
    
    /**
     * 按企业类型统计
     * 
     * @return 统计结果
     */
    List<HealthbrainEnterpriseTypeCountVo> countByType();

    /**
     * 按企业发展阶段统计
     *
     * @return 统计结果
     */
    List<HealthbrainEnterpriseTypeCountVo> countByDevelopment();
}