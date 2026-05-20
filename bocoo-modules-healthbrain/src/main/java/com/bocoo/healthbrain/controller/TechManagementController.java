package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.TechManagementBo;
import com.bocoo.healthbrain.domain.vo.TechManagementVo;
import com.bocoo.healthbrain.service.ITechManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 技术管理 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/technology")
@Tag(name = "技术管理", description = "技术管理接口")
public class TechManagementController extends BaseController {

    private final ITechManagementService techManagementService;

    /**
     * 查询技术管理列表
     */
    @SaCheckPermission("healthbrain:technology:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询技术管理列表", description = "分页查询技术管理列表,权限：healthbrain:technology:list")
    public TableDataInfo<TechManagementVo> list(
            @Parameter(description = "技术管理查询参数")
            TechManagementBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return techManagementService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询技术管理列表
     */
    @SaCheckPermission("healthbrain:technology:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询技术管理列表", description = "查询技术管理列表,权限：healthbrain:technology:list")
    public List<TechManagementVo> alllist(
            @Parameter(description = "技术管理查询参数")
            TechManagementBo bo) {
        return techManagementService.queryList(bo);
    }

    /**
     * 获取技术管理详细信息
     *
     * @param managementId 技术管理主键
     */
    @SaCheckPermission("healthbrain:technology:query")
    @GetMapping("/{managementId}")
    @Operation(summary = "获取技术管理详细信息", description = "根据主键获取技术管理详细信息,权限：healthbrain:technology:query")
    public R<TechManagementVo> getInfo(
            @Parameter(description = "技术管理主键", required = true)
            @PathVariable Long managementId) {
        return R.ok(techManagementService.getVoById(managementId));
    }

    /**
     * 新增技术管理
     */
    @SaCheckPermission("healthbrain:management:add")
    @Log(title = "技术管理", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增技术管理", description = "新增技术管理,权限：healthbrain:management:add")
    public R<Void> add(
            @Parameter(description = "技术管理信息", required = true)
            @Validated
            @RequestBody TechManagementBo bo) {
        return toAjax(techManagementService.insertByBo(bo));
    }

    /**
     * 修改技术管理
     */
    @SaCheckPermission("healthbrain:technology:edit")
    @Log(title = "技术管理", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改技术管理", description = "修改技术管理,权限：healthbrain:technology:edit")
    public R<Void> edit(
            @Parameter(description = "技术管理信息", required = true)
            @Validated
            @RequestBody TechManagementBo bo) {
        return toAjax(techManagementService.updateByBo(bo));
    }

    /**
     * 删除技术管理
     *
     * @param managementIds 技术管理主键串
     */
    @SaCheckPermission("healthbrain:technology:remove")
    @Log(title = "技术管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{managementIds}")
    @Operation(summary = "删除技术管理", description = "批量删除技术管理,权限：healthbrain:technology:remove")
    public R<Void> remove(
            @Parameter(description = "技术管理主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] managementIds) {
        return toAjax(techManagementService.deleteWithValidByIds(List.of(managementIds), true));
    }

    /**
     * 修改技术管理状态
     */
    @SaCheckPermission("healthbrain:technology:edit")
    @Log(title = "修改技术管理状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{managementId}/{status}")
    @Operation(summary = "修改技术管理状态", description = "修改技术管理状态,权限：healthbrain:technology:edit")
    public R<Void> changeStatus(
            @Parameter(description = "技术管理主键", required = true)
            @PathVariable Long managementId,
            @Parameter(description = "技术管理状态", required = true)
            @PathVariable String status) {
        return toAjax(techManagementService.updateStatus(managementId, status));
    }
}
