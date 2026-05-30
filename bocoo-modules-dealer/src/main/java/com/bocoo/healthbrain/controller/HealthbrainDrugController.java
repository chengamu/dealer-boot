package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainDrugBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainDrugPhaseCountVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainDrugVo;
import com.bocoo.healthbrain.service.IHealthbrainDrugService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 创新药管理 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/drug")
@Tag(name = "创新药管理管理", description = "创新药管理管理接口")
public class HealthbrainDrugController extends BaseController {

    private final IHealthbrainDrugService healthbrainDrugService;

    /**
     * 查询创新药管理列表
     */
    @SaCheckPermission("healthbrain:drug:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询创新药管理列表", description = "分页查询创新药管理列表,权限：healthbrain:drug:list")
    public TableDataInfo<HealthbrainDrugVo> list(
            @Parameter(description = "创新药管理查询参数")
            HealthbrainDrugBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainDrugService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询创新药管理列表
     */
    @SaCheckPermission("healthbrain:drug:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询创新药管理列表", description = "查询创新药管理列表,权限：healthbrain:drug:list")
    public List<HealthbrainDrugVo> alllist(
            @Parameter(description = "创新药管理查询参数")
            HealthbrainDrugBo bo) {
        return healthbrainDrugService.queryList(bo);
    }

    /**
     * 获取创新药管理详细信息
     *
     * @param drugId 创新药管理主键
     */
    @SaCheckPermission("healthbrain:drug:query")
    @GetMapping("/{drugId}")
    @Operation(summary = "获取创新药管理详细信息", description = "根据主键获取创新药管理详细信息,权限：healthbrain:drug:query")
    public R<HealthbrainDrugVo> getInfo(
            @Parameter(description = "创新药管理主键", required = true)
            @PathVariable Long drugId) {
        return R.ok(healthbrainDrugService.getVoById(drugId));
    }

    /**
     * 新增创新药管理
     */
    @SaCheckPermission("healthbrain:drug:add")
    @Log(title = "创新药管理", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增创新药管理", description = "新增创新药管理,权限：healthbrain:drug:add")
    public R<Void> add(
            @Parameter(description = "创新药管理信息", required = true)
            @Validated
            @RequestBody HealthbrainDrugBo bo) {
        return toAjax(healthbrainDrugService.insertByBo(bo));
    }

    /**
     * 修改创新药管理
     */
    @SaCheckPermission("healthbrain:drug:edit")
    @Log(title = "创新药管理", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改创新药管理", description = "修改创新药管理,权限：healthbrain:drug:edit")
    public R<Void> edit(
            @Parameter(description = "创新药管理信息", required = true)
            @Validated
            @RequestBody HealthbrainDrugBo bo) {
        return toAjax(healthbrainDrugService.updateByBo(bo));
    }

    /**
     * 删除创新药管理
     *
     * @param drugIds 创新药管理主键串
     */
    @SaCheckPermission("healthbrain:drug:remove")
    @Log(title = "创新药管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{drugIds}")
    @Operation(summary = "删除创新药管理", description = "批量删除创新药管理,权限：healthbrain:drug:remove")
    public R<Void> remove(
            @Parameter(description = "创新药管理主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] drugIds) {
        return toAjax(healthbrainDrugService.deleteWithValidByIds(List.of(drugIds), true));
    }

    /**
     * 按药物阶段统计数量
     */
    @SaCheckPermission("healthbrain:drug:list")
    @GetMapping("/countByPhase")
    @Operation(summary = "按药物阶段统计数量", description = "按药物阶段统计数量,权限：healthbrain:drug:list")
    public R<List<HealthbrainDrugPhaseCountVo>> phaseCount() {
        return R.ok(healthbrainDrugService.selectDrugPhaseCount());
    }
}