package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainTalentBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainTalentVo;
import com.bocoo.healthbrain.service.IHealthbrainTalentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 人才管理 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/talent")
@Tag(name = "人才管理管理", description = "人才管理管理接口")
public class HealthbrainTalentController extends BaseController {

    private final IHealthbrainTalentService healthbrainTalentService;

    /**
     * 查询人才管理列表
     */
    @SaCheckPermission("healthbrain:talent:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询人才管理列表", description = "分页查询人才管理列表,权限：healthbrain:talent:list")
    public TableDataInfo<HealthbrainTalentVo> list(
            @Parameter(description = "人才管理查询参数")
            HealthbrainTalentBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainTalentService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询人才管理列表
     */
    @SaCheckPermission("healthbrain:talent:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询人才管理列表", description = "查询人才管理列表,权限：healthbrain:talent:list")
    public List<HealthbrainTalentVo> alllist(
            @Parameter(description = "人才管理查询参数")
            HealthbrainTalentBo bo) {
        return healthbrainTalentService.queryList(bo);
    }

    /**
     * 获取人才管理详细信息
     *
     * @param talentId 人才管理主键
     */
    @SaCheckPermission("healthbrain:talent:query")
    @GetMapping("/{talentId}")
    @Operation(summary = "获取人才管理详细信息", description = "根据主键获取人才管理详细信息,权限：healthbrain:talent:query")
    public R<HealthbrainTalentVo> getInfo(
            @Parameter(description = "人才管理主键", required = true)
            @PathVariable Long talentId) {
        return R.ok(healthbrainTalentService.getVoById(talentId));
    }

    /**
     * 新增人才管理
     */
    @SaCheckPermission("healthbrain:talent:add")
    @Log(title = "人才管理", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增人才管理", description = "新增人才管理,权限：healthbrain:talent:add")
    public R<Void> add(
            @Parameter(description = "人才管理信息", required = true)
            @Validated
            @RequestBody HealthbrainTalentBo bo) {
        return toAjax(healthbrainTalentService.insertByBo(bo));
    }

    /**
     * 修改人才管理
     */
    @SaCheckPermission("healthbrain:talent:edit")
    @Log(title = "人才管理", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改人才管理", description = "修改人才管理,权限：healthbrain:talent:edit")
    public R<Void> edit(
            @Parameter(description = "人才管理信息", required = true)
            @Validated
            @RequestBody HealthbrainTalentBo bo) {
        return toAjax(healthbrainTalentService.updateByBo(bo));
    }

    /**
     * 删除人才管理
     *
     * @param talentIds 人才管理主键串
     */
    @SaCheckPermission("healthbrain:talent:remove")
    @Log(title = "人才管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{talentIds}")
    @Operation(summary = "删除人才管理", description = "批量删除人才管理,权限：healthbrain:talent:remove")
    public R<Void> remove(
            @Parameter(description = "人才管理主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] talentIds) {
        return toAjax(healthbrainTalentService.deleteWithValidByIds(List.of(talentIds), true));
    }

}
