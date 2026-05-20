package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.system.domain.bo.SysConfigBo;
import com.bocoo.system.domain.vo.SysConfigVo;
import com.bocoo.system.service.SysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 参数配置 信息操作处理
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/config")
@Tag(name = "参数配置管理", description = "参数配置信息操作处理接口")
public class SysConfigController extends BaseController {

    private final SysConfigService configService;

    /**
     * 获取参数配置列表
     */
    @SaCheckPermission("system:config:list")
    @GetMapping("/list")
    @Operation(summary = "获取参数配置列表", description = "分页获取参数配置列表")
    public TableDataInfo<SysConfigVo> list(
            @Parameter(description = "参数配置查询参数")
            SysConfigBo config,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return configService.selectPageConfigList(config, pageQuery);
    }

    /**
     * 导出参数配置列表
     */
    @Log(title = "参数管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:config:export")
    @PostMapping("/export")
    @Operation(summary = "导出参数配置列表", description = "导出参数配置列表到Excel")
    public void export(
            @Parameter(description = "参数配置查询参数")
            SysConfigBo config,
            HttpServletResponse response) {
        List<SysConfigVo> list = configService.selectConfigList(config);
        ExcelUtil.exportExcel(list, "参数数据", SysConfigVo.class, response);
    }

    /**
     * 根据参数编号获取详细信息
     *
     * @param configId 参数ID
     */
    @SaCheckPermission("system:config:query")
    @GetMapping(value = "/{configId}")
    @Operation(summary = "根据参数编号获取详细信息", description = "根据参数ID获取参数配置详细信息")
    public R<SysConfigVo> getInfo(
            @Parameter(description = "参数ID", required = true)
            @PathVariable Long configId) {
        return R.ok(configService.selectConfigById(configId));
    }

    /**
     * 根据参数键名查询参数值
     *
     * @param configKey 参数Key
     */
    @GetMapping(value = "/configKey/{configKey}")
    @Operation(summary = "根据参数键名查询参数值", description = "根据参数键名查询参数值")
    public R<Void> getConfigKey(
            @Parameter(description = "参数键名", required = true)
            @PathVariable String configKey) {
        return R.ok(configService.selectConfigByKey(configKey));
    }

    /**
     * 新增参数配置
     */
    @SaCheckPermission("system:config:add")
    @Log(title = "参数管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增参数配置", description = "新增参数配置信息")
    public R<Void> add(
            @Parameter(description = "参数配置信息", required = true)
            @Validated @RequestBody SysConfigBo config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return R.fail("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        configService.insertConfig(config);
        return R.ok();
    }

    /**
     * 修改参数配置
     */
    @SaCheckPermission("system:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改参数配置", description = "修改参数配置信息")
    public R<Void> edit(
            @Parameter(description = "参数配置信息", required = true)
            @Validated @RequestBody SysConfigBo config) {
        if (!configService.checkConfigKeyUnique(config)) {
            return R.fail("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
        }
        configService.updateConfig(config);
        return R.ok();
    }

    /**
     * 根据参数键名修改参数配置
     */
    @SaCheckPermission("system:config:edit")
    @Log(title = "参数管理", businessType = BusinessType.UPDATE)
    @PutMapping("/updateByKey")
    @Operation(summary = "根据参数键名修改参数配置", description = "根据参数键名修改参数配置")
    public R<Void> updateByKey(
            @Parameter(description = "参数配置信息", required = true)
            @RequestBody SysConfigBo config) {
        configService.updateConfig(config);
        return R.ok();
    }

    /**
     * 删除参数配置
     *
     * @param configIds 参数ID串
     */
    @SaCheckPermission("system:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{configIds}")
    @Operation(summary = "删除参数配置", description = "批量删除参数配置")
    public R<Void> remove(
            @Parameter(description = "参数ID数组", required = true)
            @PathVariable Long[] configIds) {
        configService.deleteConfigByIds(configIds);
        return R.ok();
    }

    /**
     * 刷新参数缓存
     */
    @SaCheckPermission("system:config:remove")
    @Log(title = "参数管理", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    @Operation(summary = "刷新参数缓存", description = "刷新参数缓存")
    public R<Void> refreshCache() {
        configService.resetConfigCache();
        return R.ok();
    }
}
