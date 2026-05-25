package com.bocoo.system.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bocoo.common.core.constant.CacheNames;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.common.core.utils.MapstructUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.redis.utils.CacheUtils;
import com.bocoo.common.redis.utils.RedisUtils;
import com.bocoo.common.oss.constant.OssConstant;
import com.bocoo.system.domain.entity.SysOssConfig;
import com.bocoo.system.domain.bo.SysOssConfigBo;
import com.bocoo.system.domain.vo.SysOssConfigVo;
import com.bocoo.system.mapper.SysOssConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * 对象存储配置Service业务层处理
 *
 * @author CMX
 * @date 2021-08-13
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class SysOssConfigService {

    private final SysOssConfigMapper ossConfigMapper;

    /**
     * 项目启动时，初始化参数到缓存，加载配置类
     */
    public void init() {
        List<SysOssConfig> list = ossConfigMapper.selectList();
        // 加载OSS初始化配置
        for (SysOssConfig config : list) {
            String configKey = config.getConfigKey();
            if ("1".equals(config.getStatus())) {
                RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, configKey);
            }
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
    }

    public SysOssConfigVo queryById(Long ossConfigId) {
        return ossConfigMapper.selectVoById(ossConfigId);
    }

    public TableDataInfo<SysOssConfigVo> queryPageList(SysOssConfigBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysOssConfig> lqw = buildQueryWrapper(bo);
        Page<SysOssConfigVo> result = ossConfigMapper.selectVoPage(pageQuery.build(), lqw);
        return TableDataInfo.build(result);
    }


    private LambdaQueryWrapper<SysOssConfig> buildQueryWrapper(SysOssConfigBo bo) {
        LambdaQueryWrapper<SysOssConfig> lqw = Wrappers.lambdaQuery();
        lqw.eq(StringUtils.isNotBlank(bo.getConfigKey()), SysOssConfig::getConfigKey, bo.getConfigKey());
        lqw.like(StringUtils.isNotBlank(bo.getBucketName()), SysOssConfig::getBucketName, bo.getBucketName());
        lqw.eq(StringUtils.isNotBlank(bo.getStatus()), SysOssConfig::getStatus, bo.getStatus());
        return lqw;
    }

    public Boolean insertByBo(SysOssConfigBo bo) {
        SysOssConfig config = MapstructUtils.convert(bo, SysOssConfig.class);
        validEntityBeforeSave(config);
        boolean flag = ossConfigMapper.insert(config) > 0;
        if (flag) {
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
        return flag;
    }

    public Boolean updateByBo(SysOssConfigBo bo) {
        SysOssConfig config = MapstructUtils.convert(bo, SysOssConfig.class);
        validEntityBeforeSave(config);
        LambdaUpdateWrapper<SysOssConfig> luw = new LambdaUpdateWrapper<>();
        luw.set(ObjectUtil.isNull(config.getPrefix()), SysOssConfig::getPrefix, "");
        luw.set(ObjectUtil.isNull(config.getRegion()), SysOssConfig::getRegion, "");
        luw.set(ObjectUtil.isNull(config.getExt1()), SysOssConfig::getExt1, "");
        luw.set(ObjectUtil.isNull(config.getRemark()), SysOssConfig::getRemark, "");
        luw.eq(SysOssConfig::getOssConfigId, config.getOssConfigId());
        boolean flag = ossConfigMapper.update(config, luw) > 0;
        if (flag) {
            CacheUtils.put(CacheNames.SYS_OSS_CONFIG, config.getConfigKey(), JsonUtils.toJsonString(config));
        }
        return flag;
    }

    /**
     * 保存前的数据校验
     */
    private void validEntityBeforeSave(SysOssConfig entity) {
        if (StringUtils.isNotEmpty(entity.getConfigKey()) && !checkConfigKeyUnique(entity)) {
            throw ServiceException.ofMessageKey("oss.config.key.exists", entity.getConfigKey());
        }
    }

    public Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid) {
        if (isValid) {
            if (CollUtil.containsAny(ids, OssConstant.SYSTEM_DATA_IDS)) {
                throw ServiceException.ofMessageKey("oss.config.system.delete.denied");
            }
        }
        List<SysOssConfig> list = CollUtil.newArrayList();
        for (Long configId : ids) {
            SysOssConfig config = ossConfigMapper.selectById(configId);
            list.add(config);
        }
        boolean flag = ossConfigMapper.deleteBatchIds(ids) > 0;
        if (flag) {
            list.forEach(sysOssConfig ->
                CacheUtils.evict(CacheNames.SYS_OSS_CONFIG, sysOssConfig.getConfigKey()));
        }
        return flag;
    }

    /**
     * 判断configKey是否唯一
     */
    private boolean checkConfigKeyUnique(SysOssConfig sysOssConfig) {
        long ossConfigId = ObjectUtil.isNull(sysOssConfig.getOssConfigId()) ? -1L : sysOssConfig.getOssConfigId();
        SysOssConfig info = ossConfigMapper.selectOne(new LambdaQueryWrapper<SysOssConfig>()
            .select(SysOssConfig::getOssConfigId, SysOssConfig::getConfigKey)
            .eq(SysOssConfig::getConfigKey, sysOssConfig.getConfigKey()));
        if (ObjectUtil.isNotNull(info) && info.getOssConfigId() != ossConfigId) {
            return false;
        }
        return true;
    }

    /**
     * 启用禁用状态
     */
    @Transactional(rollbackFor = Exception.class)
    public int updateOssConfigStatus(SysOssConfigBo bo) {
        SysOssConfig sysOssConfig = MapstructUtils.convert(bo, SysOssConfig.class);
        int row = ossConfigMapper.update(null, new LambdaUpdateWrapper<SysOssConfig>()
            .set(SysOssConfig::getStatus, "0"));
        row += ossConfigMapper.updateById(sysOssConfig);
        if (row > 0) {
            RedisUtils.setCacheObject(OssConstant.DEFAULT_CONFIG_KEY, sysOssConfig.getConfigKey());
        }
        return row;
    }

}
