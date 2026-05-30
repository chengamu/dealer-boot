package com.bocoo.healthbrain.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.IBaseService;
import com.bocoo.healthbrain.domain.bo.HealthbrainDrugBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainDrug;
import com.bocoo.healthbrain.domain.vo.HealthbrainDrugVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainDrugPhaseCountVo;

import java.util.Collection;
import java.util.List;

/**
 * 创新药管理 服务接口
 *
 * @author cmx
 */
public interface IHealthbrainDrugService extends IBaseService<HealthbrainDrug, HealthbrainDrugVo> {

    /**
     * 查询创新药管理列表（分页）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页数据
     */
    TableDataInfo<HealthbrainDrugVo> queryPageList(HealthbrainDrugBo bo, PageQuery pageQuery);

    /**
     * 查询创新药管理列表
     *
     * @param bo 查询条件
     * @return 创新药管理列表
     */
    List<HealthbrainDrugVo> queryList(HealthbrainDrugBo bo);

    /**
     * 按药物阶段统计数量
     *
     * @return 药物阶段统计列表
     */
    List<HealthbrainDrugPhaseCountVo> selectDrugPhaseCount();

    /**
     * 根据业务对象新增
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean insertByBo(HealthbrainDrugBo bo);

    /**
     * 根据业务对象修改
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean updateByBo(HealthbrainDrugBo bo);

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

}
