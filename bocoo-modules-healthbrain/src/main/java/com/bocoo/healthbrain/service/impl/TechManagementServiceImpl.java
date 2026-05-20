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
import com.bocoo.healthbrain.domain.bo.TechManagementBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainMilestone;
import com.bocoo.healthbrain.domain.vo.TechManagementVo;
import com.bocoo.healthbrain.domain.entity.TechManagement;
import com.bocoo.healthbrain.mapper.TechManagementMapper;
import com.bocoo.healthbrain.service.ITechManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 技术管理 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class TechManagementServiceImpl extends BaseServiceImpl<TechManagementMapper, TechManagement, TechManagementVo> implements ITechManagementService {

    /**
     * 查询技术管理列表
     *
     * @param bo 技术管理业务对象
     * @param pageQuery 分页参数
     * @return 技术管理分页数据
     */
    @Override
    public TableDataInfo<TechManagementVo> queryPageList(TechManagementBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<TechManagement> lqw = buildQueryWrapper(bo);
        Page<TechManagementVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询技术管理列表
     *
     * @param bo 技术管理业务对象
     * @return 技术管理列表
     */
    @Override
    public List<TechManagementVo> queryList(TechManagementBo bo) {
        LambdaQueryWrapper<TechManagement> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询技术管理
     *
     * @param id 技术管理主键
     * @return 技术管理信息
     */

    public TechManagementVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增技术管理
     *
     * @param bo 技术管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(TechManagementBo bo) {
        TechManagement entity = MapstructUtils.convert(bo, TechManagement.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改技术管理
     *
     * @param bo 技术管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(TechManagementBo bo) {
        TechManagement entity = MapstructUtils.convert(bo, TechManagement.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 技术管理实体
     */
    private void validEntityBeforeSave(TechManagement entity){
        //TODO 做一些数据校验,如唯一约束
        long id = ObjectUtil.isNull(entity.getId()) ? -1L : entity.getId();
        // 构建查询条件：查找除当前记录外，是否存在相同名称的记录
        LambdaQueryWrapper<TechManagement> queryWrapper = Wrappers.<TechManagement>lambdaQuery()
                .eq(TechManagement::getTitle, entity.getTitle())
                .ne(TechManagement::getId, id);
        // 检查协议名称是否已存在
        if (this.getOne(queryWrapper) != null) {
            throw new ServiceException("标题已存在[" + entity.getTitle() + "]");
        }
    }

    /**
     * 批量删除技术管理
     *
     * @param ids 需要删除的技术管理主键
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
     * @param bo 技术管理业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<TechManagement> buildQueryWrapper(TechManagementBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<TechManagement> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getTitle()), TechManagement::getTitle, bo.getTitle());
        lqw.like(StringUtils.isNotBlank(bo.getLabel()), TechManagement::getLabel, bo.getLabel());
        lqw.like(StringUtils.isNotBlank(bo.getCustomLabel()), TechManagement::getCustomLabel, bo.getCustomLabel());
        lqw.eq(StringUtils.isNotBlank(bo.getDeadline()), TechManagement::getDeadline, bo.getDeadline());
        lqw.eq(StringUtils.isNotBlank(bo.getDescription()), TechManagement::getDescription, bo.getDescription());
        lqw.like(StringUtils.isNotBlank(bo.getEnterpriseId()), TechManagement::getEnterpriseId, bo.getEnterpriseId());
        lqw.like(StringUtils.isNotBlank(bo.getContact()), TechManagement::getContact, bo.getContact());
        lqw.like(StringUtils.isNotBlank(bo.getContactInfo()), TechManagement::getContactInfo, bo.getContactInfo());
        lqw.eq(StringUtils.isNotBlank(bo.getImg()), TechManagement::getImg, bo.getImg());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), TechManagement::getStatus, bo.getStatus());
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
                new LambdaUpdateWrapper<TechManagement>()
                        .eq(TechManagement::getId, id)
                        .set(TechManagement::getStatus, status)
        );
    }

}
