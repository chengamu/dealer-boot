package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainPolicyBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainPolicyTypeCountVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainPolicyVo;
import com.bocoo.healthbrain.service.IHealthbrainPolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 政策管理 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/policy")
@Tag(name = "政策管理管理", description = "政策管理管理接口")
public class HealthbrainPolicyController extends BaseController {

    private final IHealthbrainPolicyService healthbrainPolicyService;

    /**
     * 查询政策管理列表
     */
    @SaCheckPermission("healthbrain:policy:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询政策管理列表", description = "分页查询政策管理列表,权限：healthbrain:policy:list")
    public TableDataInfo<HealthbrainPolicyVo> list(
            @Parameter(description = "政策管理查询参数")
            HealthbrainPolicyBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainPolicyService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询政策管理列表
     */
    @SaCheckPermission("healthbrain:policy:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询政策管理列表", description = "查询政策管理列表,权限：healthbrain:policy:list")
    public List<HealthbrainPolicyVo> alllist(
            @Parameter(description = "政策管理查询参数")
            HealthbrainPolicyBo bo) {
        return healthbrainPolicyService.queryList(bo);
    }

    /**
     * 获取政策管理详细信息
     *
     * @param policyId 政策管理主键
     */
    @SaCheckPermission("healthbrain:policy:query")
    @GetMapping("/{policyId}")
    @Operation(summary = "获取政策管理详细信息", description = "根据主键获取政策管理详细信息,权限：healthbrain:policy:query")
    public R<HealthbrainPolicyVo> getInfo(
            @Parameter(description = "政策管理主键", required = true)
            @PathVariable Long policyId) {
        return R.ok(healthbrainPolicyService.getVoById(policyId));
    }

    /**
     * 新增政策管理
     */
    @SaCheckPermission("healthbrain:policy:add")
    @Log(title = "政策管理", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增政策管理", description = "新增政策管理,权限：healthbrain:policy:add")
    public R<Void> add(
            @Parameter(description = "政策管理信息", required = true)
            @Validated
            @RequestBody HealthbrainPolicyBo bo) {
        return toAjax(healthbrainPolicyService.insertByBo(bo));
    }

    /**
     * 修改政策管理
     */
    @SaCheckPermission("healthbrain:policy:edit")
    @Log(title = "政策管理", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改政策管理", description = "修改政策管理,权限：healthbrain:policy:edit")
    public R<Void> edit(
            @Parameter(description = "政策管理信息", required = true)
            @Validated
            @RequestBody HealthbrainPolicyBo bo) {
        return toAjax(healthbrainPolicyService.updateByBo(bo));
    }

    /**
     * 删除政策管理
     *
     * @param policyIds 政策管理主键串
     */
    @SaCheckPermission("healthbrain:policy:remove")
    @Log(title = "政策管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{policyIds}")
    @Operation(summary = "删除政策管理", description = "批量删除政策管理,权限：healthbrain:policy:remove")
    public R<Void> remove(
            @Parameter(description = "政策管理主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] policyIds) {
        return toAjax(healthbrainPolicyService.deleteWithValidByIds(List.of(policyIds), true));
    }
    
    /**
     * 政策类型统计
     */
    @SaCheckPermission("healthbrain:policy:list")
    @GetMapping("/countByType")
    @Operation(summary = "政策类型统计", description = "政策类型统计,权限：healthbrain:policy:list")
    public R<List<HealthbrainPolicyTypeCountVo>> countByType() {
        return R.ok(healthbrainPolicyService.countByType());
    }
}
