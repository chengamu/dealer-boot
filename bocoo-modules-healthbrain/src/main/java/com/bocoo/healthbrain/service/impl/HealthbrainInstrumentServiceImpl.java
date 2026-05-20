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
import com.bocoo.healthbrain.domain.bo.HealthbrainInstrumentBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainInstrumentVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainInstrument;
import com.bocoo.healthbrain.mapper.HealthbrainInstrumentMapper;
import com.bocoo.healthbrain.service.IHealthbrainInstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 医疗器械管理 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainInstrumentServiceImpl extends BaseServiceImpl<HealthbrainInstrumentMapper, HealthbrainInstrument, HealthbrainInstrumentVo> implements IHealthbrainInstrumentService {

    /**
     * 查询医疗器械管理列表
     *
     * @param bo 医疗器械管理业务对象
     * @param pageQuery 分页参数
     * @return 医疗器械管理分页数据
     */
    @Override
    public TableDataInfo<HealthbrainInstrumentVo> queryPageList(HealthbrainInstrumentBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainInstrument> lqw = buildQueryWrapper(bo);
        Page<HealthbrainInstrumentVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询医疗器械管理列表
     *
     * @param bo 医疗器械管理业务对象
     * @return 医疗器械管理列表
     */
    @Override
    public List<HealthbrainInstrumentVo> queryList(HealthbrainInstrumentBo bo) {
        LambdaQueryWrapper<HealthbrainInstrument> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询医疗器械管理
     *
     * @param id 医疗器械管理主键
     * @return 医疗器械管理信息
     */

    public HealthbrainInstrumentVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增医疗器械管理
     *
     * @param bo 医疗器械管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainInstrumentBo bo) {
        HealthbrainInstrument entity = MapstructUtils.convert(bo, HealthbrainInstrument.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改医疗器械管理
     *
     * @param bo 医疗器械管理业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainInstrumentBo bo) {
        HealthbrainInstrument entity = MapstructUtils.convert(bo, HealthbrainInstrument.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 医疗器械管理实体
     */
    private void validEntityBeforeSave(HealthbrainInstrument entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除医疗器械管理
     *
     * @param ids 需要删除的医疗器械管理主键
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
     * @param bo 医疗器械管理业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainInstrument> buildQueryWrapper(HealthbrainInstrumentBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainInstrument> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getEnterpriseId()), HealthbrainInstrument::getEnterpriseId, bo.getEnterpriseId());
        lqw.like(StringUtils.isNotBlank(bo.getName()), HealthbrainInstrument::getName, bo.getName());
        lqw.like(StringUtils.isNotBlank(bo.getNumber()), HealthbrainInstrument::getNumber, bo.getNumber());
        lqw.like(StringUtils.isNotBlank(bo.getLevel()), HealthbrainInstrument::getLevel, bo.getLevel());
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
                new LambdaUpdateWrapper<HealthbrainInstrument>()
                        .eq(HealthbrainInstrument::getId, id)
                        .set(HealthbrainInstrument::getStatus, status)
        );*/
    }

}
