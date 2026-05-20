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
import com.bocoo.healthbrain.domain.bo.HealthbrainEnterpriseBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainEnterpriseVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainEnterprise;
import com.bocoo.healthbrain.mapper.HealthbrainEnterpriseMapper;
import com.bocoo.healthbrain.service.IHealthbrainEnterpriseService;
import com.bocoo.healthbrain.domain.vo.HealthbrainEnterpriseTypeCountVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 企业管理 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainEnterpriseServiceImpl extends BaseServiceImpl<HealthbrainEnterpriseMapper, HealthbrainEnterprise, HealthbrainEnterpriseVo> implements IHealthbrainEnterpriseService {

    /**
     * 查询企业管理列表
     *
     * @param bo 企业管理业务对象
     * @param pageQuery 分页参数
     * @return 企业管理分页数据
     */
    @Override
    public TableDataInfo<HealthbrainEnterpriseVo> queryPageList(HealthbrainEnterpriseBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainEnterprise> lqw = buildQueryWrapper(bo);
        Page<HealthbrainEnterpriseVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询企业管理列表
     *
     * @param bo 企业管理业务对象
     * @return 企业管理列表
     */
    @Override
    public List<HealthbrainEnterpriseVo> queryList(HealthbrainEnterpriseBo bo) {
        LambdaQueryWrapper<HealthbrainEnterprise> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询企业管理
     *
     * @param id 企业管理主键
     * @return 企业管理信息
     */

    public HealthbrainEnterpriseVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增企业管理
     *
     * @param bo 企业管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainEnterpriseBo bo) {
        HealthbrainEnterprise entity = MapstructUtils.convert(bo, HealthbrainEnterprise.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改企业管理
     *
     * @param bo 企业管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainEnterpriseBo bo) {
        HealthbrainEnterprise entity = MapstructUtils.convert(bo, HealthbrainEnterprise.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 企业管理实体
     */
    private void validEntityBeforeSave(HealthbrainEnterprise entity){
        //TODO 做一些数据校验,如唯一约束
        long id = ObjectUtil.isNull(entity.getId()) ? -1L : entity.getId();
        // 构建查询条件：查找除当前记录外，是否存在相同名称的记录
        LambdaQueryWrapper<HealthbrainEnterprise> queryWrapper = Wrappers.<HealthbrainEnterprise>lambdaQuery()
                .eq(HealthbrainEnterprise::getName, entity.getName())
                .ne(HealthbrainEnterprise::getId, id);
        // 检查协议名称是否已存在
        if (this.getOne(queryWrapper) != null) {
            throw new ServiceException("名称已存在[" + entity.getName() + "]");
        }
    }

    /**
     * 批量删除企业管理
     *
     * @param ids 需要删除的企业管理主键
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
     * @param bo 企业管理业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainEnterprise> buildQueryWrapper(HealthbrainEnterpriseBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainEnterprise> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getType()), HealthbrainEnterprise::getType, bo.getType());
        lqw.like(StringUtils.isNotBlank(bo.getName()), HealthbrainEnterprise::getName, bo.getName());
        lqw.like(StringUtils.isNotBlank(bo.getLogo()), HealthbrainEnterprise::getLogo, bo.getLogo());
        lqw.eq(StringUtils.isNotBlank(bo.getDevelopment()), HealthbrainEnterprise::getDevelopment, bo.getDevelopment());
        lqw.like(StringUtils.isNotBlank(bo.getAddress()), HealthbrainEnterprise::getAddress, bo.getAddress());
        lqw.eq(StringUtils.isNotBlank(bo.getMainBusiness()), HealthbrainEnterprise::getMainBusiness, bo.getMainBusiness());
        lqw.eq(StringUtils.isNotBlank(bo.getDescription()), HealthbrainEnterprise::getDescription, bo.getDescription());
        lqw.eq(StringUtils.isNotBlank(bo.getImportant()), HealthbrainEnterprise::getImportant, bo.getImportant());
        lqw.like(StringUtils.isNotBlank(bo.getContact()), HealthbrainEnterprise::getContact, bo.getContact());
        lqw.like(StringUtils.isNotBlank(bo.getContactInfo()), HealthbrainEnterprise::getContactInfo, bo.getContactInfo());
        lqw.like(StringUtils.isNotBlank(bo.getDock()), HealthbrainEnterprise::getDock, bo.getDock());
        lqw.like(StringUtils.isNotBlank(bo.getLocation()), HealthbrainEnterprise::getLocation, bo.getLocation());
        lqw.like(StringUtils.isNotBlank(bo.getSilverEconomy()), HealthbrainEnterprise::getSilverEconomy, bo.getSilverEconomy());
        lqw.like(StringUtils.isNotBlank(bo.getLifeHealth()), HealthbrainEnterprise::getLifeHealth, bo.getLifeHealth());
        lqw.like(StringUtils.isNotBlank(bo.getEnterpriseCategory()), HealthbrainEnterprise::getEnterpriseCategory, bo.getEnterpriseCategory());
        lqw.like(StringUtils.isNotBlank(bo.getProjectProgress()), HealthbrainEnterprise::getProjectProgress, bo.getProjectProgress());

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
                new LambdaUpdateWrapper<HealthbrainEnterprise>()
                        .eq(HealthbrainEnterprise::getId, id)
                        .set(HealthbrainEnterprise::getImportant, status)
        );
    }
    
    /**
     * 按企业类型统计
     * 
     * @return 统计结果
     */
    @Override
    public List<HealthbrainEnterpriseTypeCountVo> countByType() {
        return baseMapper.selectCountByType();
    }

    /**
     * 按企业发展阶段统计
     *
     * @return 统计结果
     */
    @Override
    public List<HealthbrainEnterpriseTypeCountVo> countByDevelopment() {
        return baseMapper.selectCountByDevelopment();
    }
}