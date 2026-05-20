package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.validate.AddGroup;
import com.bocoo.common.core.validate.EditGroup;
import com.bocoo.common.core.validate.QueryGroup;
import com.bocoo.common.idempotent.annotation.RepeatSubmit;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.SysOssConfigBo;
import com.bocoo.system.domain.vo.SysOssConfigVo;
import com.bocoo.system.service.SysOssConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 对象存储配置
 *
 * @author CMX
 * @date 2021-08-13
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/oss/config")
@Tag(name = "对象存储配置", description = "对象存储配置管理接口")
public class SysOssConfigController extends BaseController {

    private final SysOssConfigService sysOssConfigService;

    /**
     * 查询对象存储配置列表
     */
    @SaCheckPermission("system:oss:list")
    @GetMapping("/list")
    @Operation(summary = "查询对象存储配置列表", description = "分页查询对象存储配置列表")
    public TableDataInfo<SysOssConfigVo> list(
            @Parameter(description = "对象存储配置查询参数")
            @Validated(QueryGroup.class) SysOssConfigBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return sysOssConfigService.queryPageList(bo, pageQuery);
    }

    /**
     * 获取对象存储配置详细信息
     *
     * @param ossConfigId OSS配置ID
     */
    @SaCheckPermission("system:oss:query")
    @GetMapping("/{ossConfigId}")
    @Operation(summary = "获取对象存储配置详细信息", description = "根据OSS配置ID获取详细信息")
    public R<SysOssConfigVo> getInfo(
            @Parameter(description = "OSS配置ID", required = true)
            @NotNull(message = "主键不能为空")
            @PathVariable Long ossConfigId) {
        return R.ok(sysOssConfigService.queryById(ossConfigId));
    }

    /**
     * 新增对象存储配置
     */
    @SaCheckPermission("system:oss:add")
    @Log(title = "对象存储配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping()
    @Operation(summary = "新增对象存储配置", description = "新增对象存储配置信息")
    public R<Void> add(
            @Parameter(description = "对象存储配置信息", required = true)
            @Validated(AddGroup.class) @RequestBody SysOssConfigBo bo) {
        return toAjax(sysOssConfigService.insertByBo(bo));
    }

    /**
     * 修改对象存储配置
     */
    @SaCheckPermission("system:oss:edit")
    @Log(title = "对象存储配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping()
    @Operation(summary = "修改对象存储配置", description = "修改对象存储配置信息")
    public R<Void> edit(
            @Parameter(description = "对象存储配置信息", required = true)
            @Validated(EditGroup.class) @RequestBody SysOssConfigBo bo) {
        return toAjax(sysOssConfigService.updateByBo(bo));
    }

    /**
     * 删除对象存储配置
     *
     * @param ossConfigIds OSS配置ID串
     */
    @SaCheckPermission("system:oss:remove")
    @Log(title = "对象存储配置", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ossConfigIds}")
    @Operation(summary = "删除对象存储配置", description = "批量删除对象存储配置")
    public R<Void> remove(
            @Parameter(description = "OSS配置ID数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] ossConfigIds) {
        return toAjax(sysOssConfigService.deleteWithValidByIds(List.of(ossConfigIds), true));
    }

    /**
     * 状态修改
     */
    @SaCheckPermission("system:oss:edit")
    @Log(title = "对象存储状态修改", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    @Operation(summary = "修改对象存储配置状态", description = "修改对象存储配置状态")
    public R<Void> changeStatus(
            @Parameter(description = "对象存储配置信息", required = true)
            @RequestBody SysOssConfigBo bo) {
        return toAjax(sysOssConfigService.updateOssConfigStatus(bo));
    }
}
