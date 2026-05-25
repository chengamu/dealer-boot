package com.bocoo.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.bocoo.common.core.exception.ServiceException;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.impl.BaseServiceImpl;
import com.bocoo.demo.domain.bo.SampleBo;
import com.bocoo.demo.domain.vo.SampleVo;
import com.bocoo.demo.domain.entity.Sample;
import com.bocoo.demo.mapper.SampleMapper;
import com.bocoo.demo.service.ISampleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 样本 服务实现层
 *
 * @author validator
 */
@Service
@RequiredArgsConstructor
public class SampleServiceImpl extends BaseServiceImpl<SampleMapper, Sample, SampleVo> implements ISampleService {

    /**
     * 查询样本列表
     *
     * @param bo 样本业务对象
     * @param pageQuery 分页参数
     * @return 样本分页数据
     */
    @Override
    public TableDataInfo<SampleVo> queryPageList(SampleBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<Sample> lqw = buildQueryWrapper(bo);
        Page<SampleVo> result = pageVo(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }

    /**
     * 查询样本列表
     *
     * @param bo 样本业务对象
     * @return 样本列表
     */
    @Override
    public List<SampleVo> queryList(SampleBo bo) {
        LambdaQueryWrapper<Sample> lqw = buildQueryWrapper(bo);
        return listVo(lqw);
    }

    /**
     * 根据ID查询样本
     *
     * @param id 样本主键
     * @return 样本信息
     */

    public SampleVo queryById(Long id) {
        return getVoById(id);
    }

    /**
     * 新增样本
     *
     * @param bo 样本业务对象
     * @return 是否成功
     */
    @Override
    public Boolean insertByBo(SampleBo bo) {
        Sample entity = MapstructUtils.convert(bo, Sample.class);
        validEntityBeforeSave(entity);
        return save(entity);
    }

    /**
     * 修改样本
     *
     * @param bo 样本业务对象
     * @return 是否成功
     */
    @Override
    public Boolean updateByBo(SampleBo bo) {
        Sample entity = MapstructUtils.convert(bo, Sample.class);
        validEntityBeforeSave(entity);
        return updateById(entity);
    }

    /**
     * 保存前的数据校验
     *
     * @param entity 样本实体
     */
    private void validEntityBeforeSave(Sample entity){
        //TODO 做一些数据校验,如唯一约束
    }

    /**
     * 批量删除样本
     *
     * @param ids 需要删除的样本主键
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
     * @param bo 样本业务对象
     * @return 查询条件
     */
    private LambdaQueryWrapper<Sample> buildQueryWrapper(SampleBo bo) {
        Map<String, Object> params = bo.getParams();
        LambdaQueryWrapper<Sample> lqw = Wrappers.lambdaQuery();
        lqw.like(StringUtils.isNotBlank(bo.getSampleName()), Sample::getSampleName, bo.getSampleName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), Sample::getStatus, bo.getStatus());
        lqw.between(params.get("beginCreatedAt") != null && params.get("endCreatedAt") != null,
            Sample::getCreatedAt, params.get("beginCreatedAt"), params.get("endCreatedAt"));
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
            throw new ServiceException(MessageUtils.message("gen.validation.id.required"));
        }
        if (StringUtils.isEmpty(status)) {
            throw new ServiceException(MessageUtils.message("gen.validation.status.required"));
        }

        // 只更新状态字段
        return baseMapper.update(null,
                new LambdaUpdateWrapper<Sample>()
                        .eq(Sample::getId, id)
                        .set(Sample::getStatus, status)
        );
    }

}
