package com.bocoo.demo.service;

import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.mybatis.core.service.IBaseService;
import com.bocoo.demo.domain.bo.SampleBo;
import com.bocoo.demo.domain.entity.Sample;
import com.bocoo.demo.domain.vo.SampleVo;

import java.util.Collection;
import java.util.List;

/**
 * 样本 服务接口
 *
 * @author validator
 */
public interface ISampleService extends IBaseService<Sample, SampleVo> {

    /**
     * 查询样本列表（分页）
     *
     * @param bo        查询条件
     * @param pageQuery 分页参数
     * @return 分页数据
     */
    TableDataInfo<SampleVo> queryPageList(SampleBo bo, PageQuery pageQuery);

    /**
     * 查询样本列表
     *
     * @param bo 查询条件
     * @return 样本列表
     */
    List<SampleVo> queryList(SampleBo bo);

    /**
     * 根据业务对象新增
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean insertByBo(SampleBo bo);

    /**
     * 根据业务对象修改
     * @param bo 业务对象
     * @return 是否成功
     */
    Boolean updateByBo(SampleBo bo);

    /**
     * 批量删除并校验
     * @param ids 主键集合
     * @param isValid 是否校验
     * @return 是否成功
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

     /**
     * 修改设备分组状态
     *
     * @param id 设备分组主键
     * @param status 状态值
     * @return 更新记录数
     */
    int updateStatus(Long id, String status);

}
