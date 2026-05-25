package com.bocoo.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.collection.CollUtil;
import com.bocoo.common.core.constant.CacheConstants;
import com.bocoo.common.core.constant.CacheNames;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.json.utils.JsonUtils;
import com.bocoo.common.redis.utils.CacheUtils;
import com.bocoo.common.redis.utils.RedisUtils;
import com.bocoo.system.domain.entity.SysCache;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 缓存监控
 *
 * @author cmx
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/cache")
@Tag(name = "缓存监控", description = "缓存监控管理接口")
public class CacheController {

    private final RedissonConnectionFactory connectionFactory;

    private final static List<SysCache> CACHES = new ArrayList<>();

    static {
        CACHES.add(new SysCache(CacheConstants.ONLINE_TOKEN_KEY, "在线用户"));
        CACHES.add(new SysCache(CacheNames.SYS_CONFIG, "配置信息"));
        CACHES.add(new SysCache(CacheNames.SYS_DICT, "数据字典"));
        CACHES.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "验证码"));
        CACHES.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
        CACHES.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流处理"));
        CACHES.add(new SysCache(CacheNames.SYS_OSS_CONFIG, "OSS配置"));
        CACHES.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密码错误次数"));
    }


    /**
     * 获取缓存监控列表
     */
    @SaCheckPermission("monitor:cache:list")
    @GetMapping()
    @Operation(summary = "获取缓存监控列表", description = "获取缓存监控列表信息")
    public R<Map<String, Object>> getInfo() throws Exception {
        RedisConnection connection = connectionFactory.getConnection();
        Properties commandStats = connection.commands().info("commandstats");

        List<Map<String, String>> pieList = new ArrayList<>();
        if (commandStats != null) {
            commandStats.stringPropertyNames().forEach(key -> {
                Map<String, String> data = new HashMap<>(2);
                String property = commandStats.getProperty(key);
                data.put("name", StringUtils.removeStart(key, "cmdstat_"));
                data.put("value", StringUtils.substringBetween(property, "calls=", ",usec"));
                pieList.add(data);
            });
        }
        return R.ok(Map.of(
            "info", Objects.requireNonNull(connection.commands().info()),
            "dbSize", Objects.requireNonNull(connection.commands().dbSize()),
            "commandStats", pieList
        ));
    }

    /**
     * 获取缓存监控缓存名列表
     */
    @SaCheckPermission("monitor:cache:list")
    @GetMapping("/getNames")
    @Operation(summary = "获取缓存名称列表", description = "获取系统中所有缓存名称列表")
    public R<List<SysCache>> cache() {
        return R.ok(CACHES);
    }

    /**
     * 获取缓存监控Key列表
     *
     * @param cacheName 缓存名
     */
    @SaCheckPermission("monitor:cache:list")
    @GetMapping("/getKeys/{cacheName}")
    @Operation(summary = "获取缓存键列表", description = "根据缓存名称获取该缓存下的所有键列表")
    public R<Collection<String>> getCacheKeys(
            @Parameter(description = "缓存名称", required = true)
            @PathVariable String cacheName) {
        Collection<String> cacheKeys = new HashSet<>(0);
        if (isCacheNames(cacheName)) {
            Set<Object> keys = CacheUtils.keys(cacheName);
            if (CollUtil.isNotEmpty(keys)) {
                cacheKeys = StreamUtils.toList(keys, Object::toString);
            }
        } else {
            cacheKeys = RedisUtils.keys(cacheName + "*");
        }
        return R.ok(cacheKeys);
    }

    /**
     * 获取缓存监控缓存值详情
     *
     * @param cacheName 缓存名
     * @param cacheKey  缓存key
     */
    @SaCheckPermission("monitor:cache:list")
    @GetMapping("/getValue/{cacheName}/{cacheKey}")
    @Operation(summary = "获取缓存值详情", description = "根据缓存名称和键名获取缓存值的详细信息")
    public R<SysCache> getCacheValue(
            @Parameter(description = "缓存名称", required = true)
            @PathVariable String cacheName,
            @Parameter(description = "缓存键名", required = true)
            @PathVariable String cacheKey) {
        Object cacheValue;
        if (isCacheNames(cacheName)) {
            cacheValue = CacheUtils.get(cacheName, cacheKey);
        } else {
            cacheValue = RedisUtils.getCacheObject(cacheKey);
        }
        SysCache sysCache = new SysCache(cacheName, cacheKey, JsonUtils.toJsonString(cacheValue));
        return R.ok(sysCache);
    }

    /**
     * 清理缓存监控缓存名
     *
     * @param cacheName 缓存名
     */
    @SaCheckPermission("monitor:cache:remove")
    @DeleteMapping("/clearCacheName/{cacheName}")
    @Operation(summary = "清理指定缓存名称", description = "根据缓存名称清理该缓存下的所有数据")
    public R<Void> clearCacheName(
            @Parameter(description = "缓存名称", required = true)
            @PathVariable String cacheName) {
        if (isCacheNames(cacheName)) {
            CacheUtils.clear(cacheName);
        } else {
            RedisUtils.deleteKeys(cacheName + "*");
        }
        return R.ok();
    }

    /**
     * 清理缓存监控Key
     *
     * @param cacheKey key名
     */
    @SaCheckPermission("monitor:cache:remove")
    @DeleteMapping("/clearCacheKey/{cacheName}/{cacheKey}")
    @Operation(summary = "清理指定缓存键", description = "根据缓存名称和键名清理指定的缓存数据")
    public R<Void> clearCacheKey(
            @Parameter(description = "缓存名称", required = true)
            @PathVariable String cacheName,
            @Parameter(description = "缓存键名", required = true)
            @PathVariable String cacheKey) {
        if (isCacheNames(cacheName)) {
            CacheUtils.evict(cacheName, cacheKey);
        } else {
            RedisUtils.deleteObject(cacheKey);
        }
        return R.ok();
    }

    /**
     * 清理全部缓存监控
     */
    @SaCheckPermission("monitor:cache:remove")
    @DeleteMapping("/clearCacheAll")
    @Operation(summary = "清理全部缓存", description = "清理系统中的所有缓存数据")
    public R<Void> clearCacheAll() {
        CACHES.forEach(cache -> {
            String cacheName = cache.getCacheName();
            if (isCacheNames(cacheName)) {
                CacheUtils.clear(cacheName);
            } else {
                RedisUtils.deleteKeys(cacheName + "*");
            }
        });
        return R.ok();
    }

    private boolean isCacheNames(String cacheName) {
        return !StringUtils.contains(cacheName, ":");
    }
}
