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
import com.bocoo.healthbrain.domain.bo.HealthbrainFundTypeBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainFundTypeVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundType;
import com.bocoo.healthbrain.mapper.HealthbrainFundTypeMapper;
import com.bocoo.healthbrain.service.IHealthbrainFundTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 资金类型 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainFundTypeServiceImpl extends BaseServiceImpl<HealthbrainFundTypeMapper, HealthbrainFundType, HealthbrainFundTypeVo> implements IHealthbrainFundTypeService {

    /**
     * 查询资金类型列表
     *
     * @param bo 资金类型业务对象
     * @param pageQuery 分页参数
     * @return 资金类型分页数据
     */
    @Override
    public TableDataInfo<HealthbrainFundTypeVo> queryPageList(HealthbrainFundTypeBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainFundType> lqw = buildQueryWrapper(bo);
        Page<HealthbrainFundTypeVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询资金类型列表
     *
     * @param bo 资金类型业务对象
     * @return 资金类型列表
     */
    @Override
    public List<HealthbrainFundTypeVo> queryList(HealthbrainFundTypeBo bo) {
        LambdaQueryWrapper<HealthbrainFundType> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询资金类型
     *
     * @param id 资金类型主键
     * @return 资金类型信息
     */

    public HealthbrainFundTypeVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增资金类型
     *
     * @param bo 资金类型业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainFundTypeBo bo) {
        HealthbrainFundType entity = MapstructUtils.convert(bo, HealthbrainFundType.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改资金类型
     *
     * @param bo 资金类型业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainFundTypeBo bo) {
        HealthbrainFundType entity = MapstructUtils.convert(bo, HealthbrainFundType.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 资金类型实体
     */
    private void validEntityBeforeSave(HealthbrainFundType entity){
        //TODO 做一些数据校验,如唯一约束
        long id = ObjectUtil.isNull(entity.getId()) ? -1L : entity.getId();
        // 构建查询条件：查找除当前记录外，是否存在相同名称的记录
        LambdaQueryWrapper<HealthbrainFundType> queryWrapper = Wrappers.<HealthbrainFundType>lambdaQuery()
                .eq(HealthbrainFundType::getCode, entity.getCode())
                .ne(HealthbrainFundType::getId, id);
        // 检查协议名称是否已存在
        if (this.getOne(queryWrapper) != null) {
            throw new ServiceException("代号已存在[" + entity.getName() + "]");
        }
    }

    /**
     * 批量删除资金类型
     *
     * @param ids 需要删除的资金类型主键
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
     * @param bo 资金类型业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainFundType> buildQueryWrapper(HealthbrainFundTypeBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainFundType> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getCode()), HealthbrainFundType::getCode, bo.getCode());
        lqw.like(StringUtils.isNotBlank(bo.getName()), HealthbrainFundType::getName, bo.getName());
        lqw.like(StringUtils.isNotBlank(bo.getUnit()), HealthbrainFundType::getUnit, bo.getUnit());
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
                new LambdaUpdateWrapper<HealthbrainFundType>()
                        .eq(HealthbrainFundType::getId, id)
                        .set(HealthbrainFundType::getStatus, status)
        );*/
    }

}
