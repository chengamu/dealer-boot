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
import com.bocoo.healthbrain.domain.bo.HealthbrainMilestoneBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainEnterprise;
import com.bocoo.healthbrain.domain.vo.HealthbrainMilestoneVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainMilestone;
import com.bocoo.healthbrain.mapper.HealthbrainMilestoneMapper;
import com.bocoo.healthbrain.service.IHealthbrainMilestoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 里程碑数据 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainMilestoneServiceImpl extends BaseServiceImpl<HealthbrainMilestoneMapper, HealthbrainMilestone, HealthbrainMilestoneVo> implements IHealthbrainMilestoneService {

    /**
     * 查询里程碑数据列表
     *
     * @param bo 里程碑数据业务对象
     * @param pageQuery 分页参数
     * @return 里程碑数据分页数据
     */
    @Override
    public TableDataInfo<HealthbrainMilestoneVo> queryPageList(HealthbrainMilestoneBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainMilestone> lqw = buildQueryWrapper(bo);
        Page<HealthbrainMilestoneVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询里程碑数据列表
     *
     * @param bo 里程碑数据业务对象
     * @return 里程碑数据列表
     */
    @Override
    public List<HealthbrainMilestoneVo> queryList(HealthbrainMilestoneBo bo) {
        LambdaQueryWrapper<HealthbrainMilestone> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询里程碑数据
     *
     * @param id 里程碑数据主键
     * @return 里程碑数据信息
     */

    public HealthbrainMilestoneVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增里程碑数据
     *
     * @param bo 里程碑数据业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainMilestoneBo bo) {
        HealthbrainMilestone entity = MapstructUtils.convert(bo, HealthbrainMilestone.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改里程碑数据
     *
     * @param bo 里程碑数据业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainMilestoneBo bo) {
        HealthbrainMilestone entity = MapstructUtils.convert(bo, HealthbrainMilestone.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 里程碑数据实体
     */
    private void validEntityBeforeSave(HealthbrainMilestone entity){
        //TODO 做一些数据校验,如唯一约束

        long id = ObjectUtil.isNull(entity.getId()) ? -1L : entity.getId();
        // 构建查询条件：查找除当前记录外，是否存在相同名称的记录
        LambdaQueryWrapper<HealthbrainMilestone> queryWrapper = Wrappers.<HealthbrainMilestone>lambdaQuery()
                .eq(HealthbrainMilestone::getName, entity.getName())
                .ne(HealthbrainMilestone::getId, id);
        // 检查协议名称是否已存在
        if (this.getOne(queryWrapper) != null) {
            throw new ServiceException("名称已存在[" + entity.getName() + "]");
        }
    }

    /**
     * 批量删除里程碑数据
     *
     * @param ids 需要删除的里程碑数据主键
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
     * @param bo 里程碑数据业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainMilestone> buildQueryWrapper(HealthbrainMilestoneBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainMilestone> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getVisible()), HealthbrainMilestone::getVisible, bo.getVisible());
        lqw.like(StringUtils.isNotBlank(bo.getName()), HealthbrainMilestone::getName, bo.getName());
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
        return baseMapper.update(null,
                new LambdaUpdateWrapper<HealthbrainMilestone>()
                        .eq(HealthbrainMilestone::getId, id)
                        .set(HealthbrainMilestone::getVisible, status)
        );
    }

}
