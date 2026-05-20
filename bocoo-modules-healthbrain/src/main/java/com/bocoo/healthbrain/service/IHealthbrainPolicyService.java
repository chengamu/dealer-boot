package com.bocoo.healthbrain.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.IBaseService;
import com.bocoo.healthbrain.domain.bo.HealthbrainPolicyBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainPolicy;
import com.bocoo.healthbrain.domain.vo.HealthbrainPolicyTypeCountVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainPolicyVo;

import java.util.Collection;
import java.util.List;

/**
 * 政策管理 服务接口
 *
 * @author cmx
 */
public interface IHealthbrainPolicyService extends IBaseService<HealthbrainPolicy, HealthbrainPolicyVo> {

    /**
     * 查询政策管理列表（分页）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页数据
     */
    TableDataInfo<HealthbrainPolicyVo> queryPageList(HealthbrainPolicyBo bo, PageQuery pageQuery);

    /**
     * 查询政策管理列表
     *
     * @param bo 查询条件
     * @return 政策管理列表
     */
    List<HealthbrainPolicyVo> queryList(HealthbrainPolicyBo bo);

    /**
     * 根据业务对象新增
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean insertByBo(HealthbrainPolicyBo bo);

    /**
     * 根据业务对象修改
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean updateByBo(HealthbrainPolicyBo bo);

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
     * 按政策类型统计
     * 
     * @return 统计结果
     */
    List<HealthbrainPolicyTypeCountVo> countByType();
}