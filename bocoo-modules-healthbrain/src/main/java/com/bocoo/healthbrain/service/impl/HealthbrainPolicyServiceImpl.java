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
import com.bocoo.healthbrain.domain.bo.HealthbrainPolicyBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainPlatform;
import com.bocoo.healthbrain.domain.vo.HealthbrainPolicyVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainPolicy;
import com.bocoo.healthbrain.domain.vo.HealthbrainPolicyTypeCountVo;
import com.bocoo.healthbrain.mapper.HealthbrainPolicyMapper;
import com.bocoo.healthbrain.service.IHealthbrainPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 政策管理 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainPolicyServiceImpl extends BaseServiceImpl<HealthbrainPolicyMapper, HealthbrainPolicy, HealthbrainPolicyVo> implements IHealthbrainPolicyService {

    /**
     * 查询政策管理列表
     *
     * @param bo 政策管理业务对象
     * @param pageQuery 分页参数
     * @return 政策管理分页数据
     */
    @Override
    public TableDataInfo<HealthbrainPolicyVo> queryPageList(HealthbrainPolicyBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainPolicy> lqw = buildQueryWrapper(bo);
        Page<HealthbrainPolicyVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询政策管理列表
     *
     * @param bo 政策管理业务对象
     * @return 政策管理列表
     */
    @Override
    public List<HealthbrainPolicyVo> queryList(HealthbrainPolicyBo bo) {
        LambdaQueryWrapper<HealthbrainPolicy> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询政策管理
     *
     * @param id 政策管理主键
     * @return 政策管理信息
     */

    public HealthbrainPolicyVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增政策管理
     *
     * @param bo 政策管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainPolicyBo bo) {
        HealthbrainPolicy entity = MapstructUtils.convert(bo, HealthbrainPolicy.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改政策管理
     *
     * @param bo 政策管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainPolicyBo bo) {
        HealthbrainPolicy entity = MapstructUtils.convert(bo, HealthbrainPolicy.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 政策管理实体
     */
    private void validEntityBeforeSave(HealthbrainPolicy entity){
        //TODO 做一些数据校验,如唯一约束
        long id = ObjectUtil.isNull(entity.getId()) ? -1L : entity.getId();
        // 构建查询条件：查找除当前记录外，是否存在相同名称的记录
        LambdaQueryWrapper<HealthbrainPolicy> queryWrapper = Wrappers.<HealthbrainPolicy>lambdaQuery()
                .eq(HealthbrainPolicy::getName, entity.getName())
                .ne(HealthbrainPolicy::getId, id);
        // 检查协议名称是否已存在
        if (this.getOne(queryWrapper) != null) {
            throw new ServiceException("名称已存在[" + entity.getName() + "]");
        }
    }

    /**
     * 批量删除政策管理
     *
     * @param ids 需要删除的政策管理主键
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
     * @param bo 政策管理业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainPolicy> buildQueryWrapper(HealthbrainPolicyBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainPolicy> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getType()), HealthbrainPolicy::getType, bo.getType());
        lqw.like(StringUtils.isNotBlank(bo.getName()), HealthbrainPolicy::getName, bo.getName());
        lqw.eq(StringUtils.isNotBlank(bo.getContent()), HealthbrainPolicy::getContent, bo.getContent());
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
        return 0;/*baseMapper.update(null,
                new LambdaUpdateWrapper<HealthbrainPolicy>()
                        .eq(HealthbrainPolicy::getId, id)
                        .set(HealthbrainPolicy::getStatus, status)
        );*/
    }
    
    /**
     * 按政策类型统计
     * 
     * @return 统计结果
     */
    @Override
    public List<HealthbrainPolicyTypeCountVo> countByType() {
        return baseMapper.selectCountByType();
    }
}
