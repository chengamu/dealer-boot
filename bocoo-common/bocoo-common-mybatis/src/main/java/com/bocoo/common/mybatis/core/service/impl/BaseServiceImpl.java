package com.bocoo.common.mybatis.core.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bocoo.common.mybatis.core.mapper.BaseMapperPlus;
import com.bocoo.common.mybatis.core.service.IBaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * 基础服务实现类 (优化版)
 * <p>
 * 继承 ServiceImpl 获取标准 CRUD 功能，并实现 IBaseService 接口。
 * 核心职责是作为服务层和 Mapper 层的桥梁，并将复杂操作委托给 BaseMapperPlus。
 * </p>
 *
 * @param <M> Mapper类型 (必须继承自 BaseMapperPlus)
 * @param <T> 实体类型
 * @param <V> VO类型
 * @author cmx
 */
public abstract class BaseServiceImpl<M extends BaseMapperPlus<T, V>, T , V>
        extends ServiceImpl<M, T> implements IBaseService<T, V> {


    protected Class<V> voClass = currentVoClass();

    @Override
    public Class<V> getVoClass() {
        return voClass;
    }

    @SuppressWarnings("unchecked")
    protected Class<V> currentVoClass() {
        // 使用MP的ReflectionKit从子类的泛型中解析出VO类型
        return (Class<V>) ReflectionKit.getSuperClassGenericType(getClass(), BaseServiceImpl.class, 2);
    }


    /**
     * 重写批量保存方法，使其委托给 BaseMapperPlus 中更简洁的实现。
     * ServiceImpl 默认使用 SqlHelper 逐条执行，而 BaseMapperPlus 使用 Db 工具类，
     * 两者底层机制相似，但此处重写可保持与 Mapper 层行为的绝对一致性。
     *
     * @param entityList 实体对象集合
     * @param batchSize  批量大小
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveBatch(Collection<T> entityList, int batchSize) {
        // 委托给 BaseMapperPlus 的实现，它内部使用 Db.saveBatch，更简洁
        return baseMapper.insertBatch(entityList, batchSize);
    }

    /**
     * 重写批量更新方法，使其委托给 BaseMapperPlus 中的实现。
     *
     * @param entityList 实体对象集合
     * @param batchSize  批量大小
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateBatchById(Collection<T> entityList, int batchSize) {
        return baseMapper.updateBatchById(entityList, batchSize);
    }

    /**
     * 重写批量保存或更新方法，使其委托给 BaseMapperPlus 中的实现。
     *
     * @param entityList 实体对象集合
     * @param batchSize  批量大小
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveOrUpdateBatch(Collection<T> entityList, int batchSize) {
        return baseMapper.insertOrUpdateBatch(entityList, batchSize);
    }


}