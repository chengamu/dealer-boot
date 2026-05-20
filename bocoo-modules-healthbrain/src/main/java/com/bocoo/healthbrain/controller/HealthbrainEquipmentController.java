package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainEquipmentBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainEquipmentVo;
import com.bocoo.healthbrain.service.IHealthbrainEquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 共享设备管理 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/equipment")
@Tag(name = "共享设备管理管理", description = "共享设备管理管理接口")
public class HealthbrainEquipmentController extends BaseController {

    private final IHealthbrainEquipmentService healthbrainEquipmentService;

    /**
     * 查询共享设备管理列表
     */
    @SaCheckPermission("healthbrain:equipment:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询共享设备管理列表", description = "分页查询共享设备管理列表,权限：healthbrain:equipment:list")
    public TableDataInfo<HealthbrainEquipmentVo> list(
            @Parameter(description = "共享设备管理查询参数")
            HealthbrainEquipmentBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainEquipmentService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询共享设备管理列表
     */
    @SaCheckPermission("healthbrain:equipment:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询共享设备管理列表", description = "查询共享设备管理列表,权限：healthbrain:equipment:list")
    public List<HealthbrainEquipmentVo> alllist(
            @Parameter(description = "共享设备管理查询参数")
            HealthbrainEquipmentBo bo) {
        return healthbrainEquipmentService.queryList(bo);
    }

    /**
     * 获取共享设备管理详细信息
     *
     * @param equipmentId 共享设备管理主键
     */
    @SaCheckPermission("healthbrain:equipment:query")
    @GetMapping("/{equipmentId}")
    @Operation(summary = "获取共享设备管理详细信息", description = "根据主键获取共享设备管理详细信息,权限：healthbrain:equipment:query")
    public R<HealthbrainEquipmentVo> getInfo(
            @Parameter(description = "共享设备管理主键", required = true)
            @PathVariable Long equipmentId) {
        return R.ok(healthbrainEquipmentService.getVoById(equipmentId));
    }

    /**
     * 新增共享设备管理
     */
    @SaCheckPermission("healthbrain:equipment:add")
    @Log(title = "共享设备管理", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增共享设备管理", description = "新增共享设备管理,权限：healthbrain:equipment:add")
    public R<Void> add(
            @Parameter(description = "共享设备管理信息", required = true)
            @Validated
            @RequestBody HealthbrainEquipmentBo bo) {
        return toAjax(healthbrainEquipmentService.insertByBo(bo));
    }

    /**
     * 修改共享设备管理
     */
    @SaCheckPermission("healthbrain:equipment:edit")
    @Log(title = "共享设备管理", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改共享设备管理", description = "修改共享设备管理,权限：healthbrain:equipment:edit")
    public R<Void> edit(
            @Parameter(description = "共享设备管理信息", required = true)
            @Validated
            @RequestBody HealthbrainEquipmentBo bo) {
        return toAjax(healthbrainEquipmentService.updateByBo(bo));
    }

    /**
     * 删除共享设备管理
     *
     * @param equipmentIds 共享设备管理主键串
     */
    @SaCheckPermission("healthbrain:equipment:remove")
    @Log(title = "共享设备管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{equipmentIds}")
    @Operation(summary = "删除共享设备管理", description = "批量删除共享设备管理,权限：healthbrain:equipment:remove")
    public R<Void> remove(
            @Parameter(description = "共享设备管理主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] equipmentIds) {
        return toAjax(healthbrainEquipmentService.deleteWithValidByIds(List.of(equipmentIds), true));
    }

}
