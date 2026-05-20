package com.bocoo.healthbrain.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bocoo.common.core.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.impl.BaseServiceImpl;
import com.bocoo.healthbrain.domain.bo.HealthbrainEquipmentBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainEquipmentVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainEquipment;
import com.bocoo.healthbrain.mapper.HealthbrainEquipmentMapper;
import com.bocoo.healthbrain.service.IHealthbrainEquipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 共享设备管理 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainEquipmentServiceImpl extends BaseServiceImpl<HealthbrainEquipmentMapper, HealthbrainEquipment, HealthbrainEquipmentVo> implements IHealthbrainEquipmentService {

    /**
     * 查询共享设备管理列表
     *
     * @param bo 共享设备管理业务对象
     * @param pageQuery 分页参数
     * @return 共享设备管理分页数据
     */
    @Override
    public TableDataInfo<HealthbrainEquipmentVo> queryPageList(HealthbrainEquipmentBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainEquipment> lqw = buildQueryWrapper(bo);
        Page<HealthbrainEquipmentVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询共享设备管理列表
     *
     * @param bo 共享设备管理业务对象
     * @return 共享设备管理列表
     */
    @Override
    public List<HealthbrainEquipmentVo> queryList(HealthbrainEquipmentBo bo) {
        LambdaQueryWrapper<HealthbrainEquipment> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询共享设备管理
     *
     * @param id 共享设备管理主键
     * @return 共享设备管理信息
     */

    public HealthbrainEquipmentVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增共享设备管理
     *
     * @param bo 共享设备管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainEquipmentBo bo) {
        HealthbrainEquipment entity = MapstructUtils.convert(bo, HealthbrainEquipment.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改共享设备管理
     *
     * @param bo 共享设备管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainEquipmentBo bo) {
        HealthbrainEquipment entity = MapstructUtils.convert(bo, HealthbrainEquipment.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 共享设备管理实体
     */
    private void validEntityBeforeSave(HealthbrainEquipment entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除共享设备管理
     *
     * @param ids 需要删除的共享设备管理主键
     * @param isValid 是否校验
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if(isValid){
            //TODO 做一些业务上的校验,判断是否需要校验
        }
        return removeByIds(ids);
    }

    /**
     * 构建查询条件
     *
     * @param bo 共享设备管理业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainEquipment> buildQueryWrapper(HealthbrainEquipmentBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainEquipment> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), HealthbrainEquipment::getName, bo.getName());
        lqw.like(StringUtils.isNotBlank(bo.getBrand()), HealthbrainEquipment::getBrand, bo.getBrand());
        lqw.like(StringUtils.isNotBlank(bo.getSpecification()), HealthbrainEquipment::getSpecification, bo.getSpecification());
        lqw.like(StringUtils.isNotBlank(bo.getNumber()), HealthbrainEquipment::getNumber, bo.getNumber());
        lqw.eq(StringUtils.isNotBlank(bo.getService()), HealthbrainEquipment::getService, bo.getService());
        lqw.like(StringUtils.isNotBlank(bo.getEnterpriseId()), HealthbrainEquipment::getEnterpriseId, bo.getEnterpriseId());
        lqw.like(StringUtils.isNotBlank(bo.getEnterpriseName()), HealthbrainEquipment::getEnterpriseName, bo.getEnterpriseName());
       // lqw.like(StringUtils.isNotBlank(bo.getUsageRate()), HealthbrainEquipment::getUsageRate, bo.getUsageRate());
        return lqw;
    }

    /**
     * 修改设备分组状态
     *
     * @param id 设备分组主键
     * @param status 状态值
     * @return 更新记录数
     */
    @Override
    public int updateStatus(Long id, String status) {
        // 参数校验
        if (id == null) {
            throw new ServiceException("ID不能为空");
        }
        if (StringUtils.isEmpty(status)) {
            throw new ServiceException("状态值不能为空");
        }

        // 只更新状态字段
        return 0;
    }

}
