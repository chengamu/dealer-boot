package com.bocoo.common.mybatis.core.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.IService;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;

import java.io.Serializable;
import java.util.List;

/**
 * 基础服务接口 (优化版)
 * 继承 Mybatis-Plus 的 IService 以复用其标准接口定义，并扩展 VO 相关方法。
 *
 * @param <T> 实体类型
 * @param <V> VO类型
 */
public interface IBaseService<T , V> extends IService<T> {

    /**
     * 获取对应 entity 的 BaseMapperPlus
     * <p>
     * 关键改动：返回类型为 BaseMapperPlus，以原生支持其扩展的 VO 查询和批量方法。
     *
     * @return BaseMapperPlus
     */
    @Override
    BaseMapperPlus<T, V> getBaseMapper();

    /**
     * 获取 vo 的 class
     *
     * @return {@link Class<V>}
     */
    Class<V> getVoClass();

    /**
     * 根据 ID 查询 VO
     *
     * @param id 主键ID
     * @return V a VO
     */
    default V getVoById(Serializable id) {
        return getBaseMapper().selectVoById(id);
    }

    /**
     * 根据 Wrapper，查询一条VO记录
     *
     * @param queryWrapper 实体对象封装操作类
     * @return V a VO
     */
    default V getVoOne(Wrapper<T> queryWrapper) {
        return getBaseMapper().selectVoOne(queryWrapper);
    }

    /**
     * 查询所有VO
     *
     * @return VO 列表
     */
    default List<V> listVo() {
        return getBaseMapper().selectVoList();
    }

    /**
     * 查询VO列表
     *
     * @param queryWrapper 实体对象封装操作类
     * @return VO 列表
     */
    default List<V> listVo(Wrapper<T> queryWrapper) {
        return getBaseMapper().selectVoList(queryWrapper);
    }

    /**
     * 翻页查询 VO
     *
     * @param page         翻页对象 (输入类型为 IPage<T> 以接收查询参数)
     * @param queryWrapper 实体对象封装操作类
     * @param <P>          IPage 的实现类
     * @return IPage<V>
     */
    default <P extends IPage<V>> P pageVo(IPage<T> page, Wrapper<T> queryWrapper) {
        return getBaseMapper().selectVoPage(page, queryWrapper);
    }

    /**
     * 翻页查询 VO (无条件)
     *
     * @param page 翻页对象 (输入类型为 IPage<T> 以接收查询参数)
     * @param <P>  IPage 的实现类
     * @return IPage<V>
     */
    default <P extends IPage<V>> P pageVo(IPage<T> page) {
        return getBaseMapper().selectVoPage(page, Wrappers.emptyWrapper());
    }
}