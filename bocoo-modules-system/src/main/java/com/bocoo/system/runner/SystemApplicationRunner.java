package com.bocoo.system.runner;

import com.bocoo.common.core.config.BocooConfig;
import com.bocoo.system.service.SysConfigService;
import com.bocoo.system.service.SysDictTypeService;
import com.bocoo.system.service.SysOssConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 初始化 system 模块对应业务数据
 *
 * @author Lion Li
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class SystemApplicationRunner implements ApplicationRunner {

    private final BocooConfig bocooConfig;
    private final SysConfigService configService;
    private final SysDictTypeService dictTypeService;
    private final SysOssConfigService ossConfigService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        ossConfigService.init();
        log.info("初始化OSS配置成功");
        if (bocooConfig.isCacheLazy()) {
            return;
        }
        configService.loadingConfigCache();
        log.info("加载参数缓存数据成功");
        dictTypeService.loadingDictCache();
        log.info("加载字典缓存数据成功");

    }

}
