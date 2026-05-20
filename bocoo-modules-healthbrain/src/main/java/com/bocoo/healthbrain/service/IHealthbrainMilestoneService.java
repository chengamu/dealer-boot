package com.bocoo.healthbrain.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.IBaseService;
import com.bocoo.healthbrain.domain.bo.HealthbrainMilestoneBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainMilestone;
import com.bocoo.healthbrain.domain.vo.HealthbrainMilestoneVo;

import java.util.Collection;
import java.util.List;

/**
 * 里程碑数据 服务接口
 *
 * @author cmx
 */
public interface IHealthbrainMilestoneService extends IBaseService<HealthbrainMilestone, HealthbrainMilestoneVo> {

    /**
     * 查询里程碑数据列表（分页）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页数据
     */
    TableDataInfo<HealthbrainMilestoneVo> queryPageList(HealthbrainMilestoneBo bo, PageQuery pageQuery);

    /**
     * 查询里程碑数据列表
     *
     * @param bo 查询条件
     * @return 里程碑数据列表
     */
    List<HealthbrainMilestoneVo> queryList(HealthbrainMilestoneBo bo);

    /**
     * 根据业务对象新增
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean insertByBo(HealthbrainMilestoneBo bo);

    /**
     * 根据业务对象修改
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean updateByBo(HealthbrainMilestoneBo bo);

    /**
     * 批量删除并校验
     * @param ids 主键集合
     * @param isValid 是否校验
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

     /**
     * 修改状态
     *
     * @param id   主键
     * @param status 状态值
     * @return 更新记录数
     */
    int updateStatus(Long id, String status);

}
