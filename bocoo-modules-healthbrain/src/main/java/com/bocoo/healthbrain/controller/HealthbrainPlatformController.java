package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainPlatformBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainPlatformVo;
import com.bocoo.healthbrain.service.IHealthbrainPlatformService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 平台管理 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/platform")
@Tag(name = "平台管理管理", description = "平台管理管理接口")
public class HealthbrainPlatformController extends BaseController {

    private final IHealthbrainPlatformService healthbrainPlatformService;

    /**
     * 查询平台管理列表
     */
    @SaCheckPermission("healthbrain:platform:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询平台管理列表", description = "分页查询平台管理列表,权限：healthbrain:platform:list")
    public TableDataInfo<HealthbrainPlatformVo> list(
            @Parameter(description = "平台管理查询参数")
            HealthbrainPlatformBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainPlatformService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询平台管理列表
     */
    @SaCheckPermission("healthbrain:platform:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询平台管理列表", description = "查询平台管理列表,权限：healthbrain:platform:list")
    public List<HealthbrainPlatformVo> alllist(
            @Parameter(description = "平台管理查询参数")
            HealthbrainPlatformBo bo) {
        return healthbrainPlatformService.queryList(bo);
    }

    /**
     * 获取平台管理详细信息
     *
     * @param platformId 平台管理主键
     */
    @SaCheckPermission("healthbrain:platform:query")
    @GetMapping("/{platformId}")
    @Operation(summary = "获取平台管理详细信息", description = "根据主键获取平台管理详细信息,权限：healthbrain:platform:query")
    public R<HealthbrainPlatformVo> getInfo(
            @Parameter(description = "平台管理主键", required = true)
            @PathVariable Long platformId) {
        return R.ok(healthbrainPlatformService.getVoById(platformId));
    }

    /**
     * 新增平台管理
     */
    @SaCheckPermission("healthbrain:platform:add")
    @Log(title = "平台管理", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增平台管理", description = "新增平台管理,权限：healthbrain:platform:add")
    public R<Void> add(
            @Parameter(description = "平台管理信息", required = true)
            @Validated
            @RequestBody HealthbrainPlatformBo bo) {
        return toAjax(healthbrainPlatformService.insertByBo(bo));
    }

    /**
     * 修改平台管理
     */
    @SaCheckPermission("healthbrain:platform:edit")
    @Log(title = "平台管理", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改平台管理", description = "修改平台管理,权限：healthbrain:platform:edit")
    public R<Void> edit(
            @Parameter(description = "平台管理信息", required = true)
            @Validated
            @RequestBody HealthbrainPlatformBo bo) {
        return toAjax(healthbrainPlatformService.updateByBo(bo));
    }

    /**
     * 删除平台管理
     *
     * @param platformIds 平台管理主键串
     */
    @SaCheckPermission("healthbrain:platform:remove")
    @Log(title = "平台管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{platformIds}")
    @Operation(summary = "删除平台管理", description = "批量删除平台管理,权限：healthbrain:platform:remove")
    public R<Void> remove(
            @Parameter(description = "平台管理主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] platformIds) {
        return toAjax(healthbrainPlatformService.deleteWithValidByIds(List.of(platformIds), true));
    }

}
