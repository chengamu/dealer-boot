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
import com.bocoo.healthbrain.domain.bo.HealthbrainFundDataBo;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundType;
import com.bocoo.healthbrain.domain.vo.HealthbrainFundDataVo;
import com.bocoo.healthbrain.domain.entity.HealthbrainFundData;
import com.bocoo.healthbrain.mapper.HealthbrainFundDataMapper;
import com.bocoo.healthbrain.service.IHealthbrainFundDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 资金数据 服务实现层
 *
 * @author cmx
 */
@Service
@RequiredArgsConstructor
public class HealthbrainFundDataServiceImpl extends BaseServiceImpl<HealthbrainFundDataMapper, HealthbrainFundData, HealthbrainFundDataVo> implements IHealthbrainFundDataService {

    /**
     * 查询资金数据列表
     *
     * @param bo 资金数据业务对象
     * @param pageQuery 分页参数
     * @return 资金数据分页数据
     */
    @Override
    public TableDataInfo<HealthbrainFundDataVo> queryPageList(HealthbrainFundDataBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HealthbrainFundData> lqw = buildQueryWrapper(bo);
        Page<HealthbrainFundDataVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询资金数据列表
     *
     * @param bo 资金数据业务对象
     * @return 资金数据列表
     */
    @Override
    public List<HealthbrainFundDataVo> queryList(HealthbrainFundDataBo bo) {
        LambdaQueryWrapper<HealthbrainFundData> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询资金数据
     *
     * @param id 资金数据主键
     * @return 资金数据信息
     */

    public HealthbrainFundDataVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增资金数据
     *
     * @param bo 资金数据业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(HealthbrainFundDataBo bo) {
        HealthbrainFundData entity = MapstructUtils.convert(bo, HealthbrainFundData.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改资金数据
     *
     * @param bo 资金数据业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(HealthbrainFundDataBo bo) {
        HealthbrainFundData entity = MapstructUtils.convert(bo, HealthbrainFundData.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 资金数据实体
     */
    private void validEntityBeforeSave(HealthbrainFundData entity){
        //TODO 做一些数据校验,如唯一约束
        long id = ObjectUtil.isNull(entity.getId()) ? -1L : entity.getId();
        // 构建查询条件：查找除当前记录外，是否存在相同名称的记录
        LambdaQueryWrapper<HealthbrainFundData> queryWrapper = Wrappers.<HealthbrainFundData>lambdaQuery()
                .eq(HealthbrainFundData::getYear, entity.getYear())
                .eq(HealthbrainFundData::getFundTypeId, entity.getFundTypeId())
                .ne(HealthbrainFundData::getId, id);
        // 检查协议名称是否已存在
        if (this.getOne(queryWrapper) != null) {
            throw new ServiceException("年份已存在[" + entity.getYear() + "]");
        }
    }

    /**
     * 批量删除资金数据
     *
     * @param ids 需要删除的资金数据主键
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
     * @param bo 资金数据业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<HealthbrainFundData> buildQueryWrapper(HealthbrainFundDataBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<HealthbrainFundData> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getFundTypeId()), HealthbrainFundData::getFundTypeId, bo.getFundTypeId());
        lqw.like(StringUtils.isNotBlank(bo.getYear()), HealthbrainFundData::getYear, bo.getYear());
      //  lqw.like(StringUtils.isNotBlank(bo.getValue()), HealthbrainFundData::getValue, bo.getValue());
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
                new LambdaUpdateWrapper<HealthbrainFundData>()
                        .eq(HealthbrainFundData::getId, id)
                        .set(HealthbrainFundData::getStatus, status)
        );*/
    }

}
