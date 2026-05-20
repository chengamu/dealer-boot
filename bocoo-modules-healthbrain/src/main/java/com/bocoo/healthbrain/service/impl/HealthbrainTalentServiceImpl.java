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
import com.bocoo.healthbrain.domain.bo.HealthbrainTalentBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainTalentVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainTalent;
import com.bocoo.healthbrain.mapper.HealthbrainTalentMapper;
import com.bocoo.healthbrain.service.IHealthbrainTalentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 人才管理 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainTalentServiceImpl extends BaseServiceImpl<HealthbrainTalentMapper, HealthbrainTalent, HealthbrainTalentVo> implements IHealthbrainTalentService {

    /**
     * 查询人才管理列表
     *
     * @param bo 人才管理业务对象
     * @param pageQuery 分页参数
     * @return 人才管理分页数据
     */
    @Override
    public TableDataInfo<HealthbrainTalentVo> queryPageList(HealthbrainTalentBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainTalent> lqw = buildQueryWrapper(bo);
        Page<HealthbrainTalentVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询人才管理列表
     *
     * @param bo 人才管理业务对象
     * @return 人才管理列表
     */
    @Override
    public List<HealthbrainTalentVo> queryList(HealthbrainTalentBo bo) {
        LambdaQueryWrapper<HealthbrainTalent> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询人才管理
     *
     * @param id 人才管理主键
     * @return 人才管理信息
     */

    public HealthbrainTalentVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增人才管理
     *
     * @param bo 人才管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainTalentBo bo) {
        HealthbrainTalent entity = MapstructUtils.convert(bo, HealthbrainTalent.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改人才管理
     *
     * @param bo 人才管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainTalentBo bo) {
        HealthbrainTalent entity = MapstructUtils.convert(bo, HealthbrainTalent.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 人才管理实体
     */
    private void validEntityBeforeSave(HealthbrainTalent entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除人才管理
     *
     * @param ids 需要删除的人才管理主键
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
     * @param bo 人才管理业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainTalent> buildQueryWrapper(HealthbrainTalentBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainTalent> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getType()), HealthbrainTalent::getType, bo.getType());
        lqw.like(StringUtils.isNotBlank(bo.getName()), HealthbrainTalent::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getDevelopment()), HealthbrainTalent::getDevelopment, bo.getDevelopment());
        lqw.like(StringUtils.isNotBlank(bo.getEnterpriseId()), HealthbrainTalent::getEnterpriseId, bo.getEnterpriseId());
        lqw.like(StringUtils.isNotBlank(bo.getEnterprise()), HealthbrainTalent::getEnterprise, bo.getEnterprise());
        lqw.like(StringUtils.isNotBlank(bo.getPosition()), HealthbrainTalent::getPosition, bo.getPosition());
        lqw.eq(StringUtils.isNotBlank(bo.getHonor()), HealthbrainTalent::getHonor, bo.getHonor());
        lqw.eq(StringUtils.isNotBlank(bo.getExperience()), HealthbrainTalent::getExperience, bo.getExperience());
        lqw.eq(StringUtils.isNotBlank(bo.getAvatar()), HealthbrainTalent::getAvatar, bo.getAvatar());
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
        return 0; /*baseMapper.update(null,
                new LambdaUpdateWrapper<HealthbrainTalent>()
                        .eq(HealthbrainTalent::getId, id)
                        .set(HealthbrainTalent::getStatus, status)
        );*/
    }

}
