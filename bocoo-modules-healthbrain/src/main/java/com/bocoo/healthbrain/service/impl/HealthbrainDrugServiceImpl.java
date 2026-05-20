package com.bocoo.healthbrain.service.impl;

import cn.hutool.core.util.ObjectUtil;
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
import com.bocoo.healthbrain.domain.bo.HealthbrainDrugBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainEnterprise;
import com.bocoo.healthbrain.domain.vo.HealthbrainDrugPhaseCountVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainDrugVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainDrug;
import com.bocoo.healthbrain.mapper.HealthbrainDrugMapper;
import com.bocoo.healthbrain.service.IHealthbrainDrugService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 创新药管理 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainDrugServiceImpl extends BaseServiceImpl<HealthbrainDrugMapper, HealthbrainDrug, HealthbrainDrugVo> implements IHealthbrainDrugService {

    /**
     * 查询创新药管理列表
     *
     * @param bo 创新药管理业务对象
     * @param pageQuery 分页参数
     * @return 创新药管理分页数据
     */
    @Override
    public TableDataInfo<HealthbrainDrugVo> queryPageList(HealthbrainDrugBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainDrug> lqw = buildQueryWrapper(bo);
        Page<HealthbrainDrugVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询创新药管理列表
     *
     * @param bo 创新药管理业务对象
     * @return 创新药管理列表
     */
    @Override
    public List<HealthbrainDrugVo> queryList(HealthbrainDrugBo bo) {
        LambdaQueryWrapper<HealthbrainDrug> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询创新药管理
     *
     * @param id 创新药管理主键
     * @return 创新药管理信息
     */
    public HealthbrainDrugVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增创新药管理
     *
     * @param bo 创新药管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainDrugBo bo) {
        HealthbrainDrug entity = MapstructUtils.convert(bo, HealthbrainDrug.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改创新药管理
     *
     * @param bo 创新药管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainDrugBo bo) {
        HealthbrainDrug entity = MapstructUtils.convert(bo, HealthbrainDrug.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 创新药管理实体
     */
    private void validEntityBeforeSave(HealthbrainDrug entity) {
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除创新药管理
     *
     * @param ids     需要删除的创新药管理主键
     * @param isValid 是否校验
     * @return 是否成功
     */
    @Override
    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            // 做一些业务上的校验,判断是否需要校验
        }
        return removeByIds(ids);
    }

    /**
     * 构建查询条件
     *
     * @param bo 创新药管理业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainDrug> buildQueryWrapper(HealthbrainDrugBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainDrug> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getName()), HealthbrainDrug::getName, bo.getName());
        lqw.like(StringUtils.isNotBlank(bo.getEnterpriseId()), HealthbrainDrug::getEnterpriseId, bo.getEnterpriseId());
        lqw.eq(StringUtils.isNotBlank(bo.getPhase()), HealthbrainDrug::getPhase, bo.getPhase());
        lqw.like(StringUtils.isNotBlank(bo.getIndication()), HealthbrainDrug::getIndication, bo.getIndication());
        return lqw;
    }

    /**
     * 按药物阶段统计数量
     *
     * @return 药物阶段统计列表
     */
    @Override
    public List<HealthbrainDrugPhaseCountVo> selectDrugPhaseCount() {
        return baseMapper.selectDrugPhaseCount();
    }

    /**
     * 修改创新药状态
     *
     * @param id     创新药主键
     * @param status 状态值
     * @return 更新记录数
     */
    @Override
    public int updateStatus(Long id, String status) {
        // 由于实体类中没有 status 字段，这里直接返回0
        return 0;
    }
}