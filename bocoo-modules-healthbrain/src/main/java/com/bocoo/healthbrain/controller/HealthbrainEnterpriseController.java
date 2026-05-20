package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainEnterpriseBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainEnterpriseVo;
import com.bocoo.healthbrain.domain.vo.HealthbrainEnterpriseTypeCountVo;
import com.bocoo.healthbrain.service.IHealthbrainEnterpriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 企业管理 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/enterprise")
@Tag(name = "企业管理管理", description = "企业管理管理接口")
public class HealthbrainEnterpriseController extends BaseController {

    private final IHealthbrainEnterpriseService healthbrainEnterpriseService;

    /**
     * 查询企业管理列表
     */
    @SaCheckPermission("healthbrain:enterprise:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询企业管理列表", description = "分页查询企业管理列表,权限：healthbrain:enterprise:list")
    public TableDataInfo<HealthbrainEnterpriseVo> list(
            @Parameter(description = "企业管理查询参数")
            HealthbrainEnterpriseBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainEnterpriseService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询企业管理列表
     */
    @SaCheckPermission("healthbrain:enterprise:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询企业管理列表", description = "查询企业管理列表,权限：healthbrain:enterprise:list")
    public List<HealthbrainEnterpriseVo> alllist(
            @Parameter(description = "企业管理查询参数")
            HealthbrainEnterpriseBo bo) {
        return healthbrainEnterpriseService.queryList(bo);
    }

    /**
     * 获取企业管理详细信息
     *
     * @param enterpriseId 企业管理主键
     */
    @SaCheckPermission("healthbrain:enterprise:query")
    @GetMapping("/{enterpriseId}")
    @Operation(summary = "获取企业管理详细信息", description = "根据主键获取企业管理详细信息,权限：healthbrain:enterprise:query")
    public R<HealthbrainEnterpriseVo> getInfo(
            @Parameter(description = "企业管理主键", required = true)
            @PathVariable Long enterpriseId) {
        return R.ok(healthbrainEnterpriseService.getVoById(enterpriseId));
    }

    /**
     * 新增企业管理
     */
    @SaCheckPermission("healthbrain:enterprise:add")
    @Log(title = "企业管理", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增企业管理", description = "新增企业管理,权限：healthbrain:enterprise:add")
    public R<Void> add(
            @Parameter(description = "企业管理信息", required = true)
            @Validated
            @RequestBody HealthbrainEnterpriseBo bo) {
        return toAjax(healthbrainEnterpriseService.insertByBo(bo));
    }

    /**
     * 修改企业管理
     */
    @SaCheckPermission("healthbrain:enterprise:edit")
    @Log(title = "企业管理", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改企业管理", description = "修改企业管理,权限：healthbrain:enterprise:edit")
    public R<Void> edit(
            @Parameter(description = "企业管理信息", required = true)
            @Validated
            @RequestBody HealthbrainEnterpriseBo bo) {
        return toAjax(healthbrainEnterpriseService.updateByBo(bo));
    }

    /**
     * 修改企业状态
     *
     * @param Id 企业ID，用于标识要修改状态的企业
     * @param status 企业状态，表示要设置的企业状态值
     * @return R<Void> 统一响应结果，包含操作是否成功的状态信息
     */
    @SaCheckPermission("healthbrain:enterprise:edit")
    @Log(title = "修改企业状态", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus/{Id}/{status}")
    @Operation(summary = "修改企业状态", description = "修改企业状态,权限：healthbrain:enterprise:edit")
    public R<Void> changeStatus(
            @Parameter(description = "ID", required = true)
            @PathVariable Long Id,
            @Parameter(description = "状态", required = true)
            @PathVariable String status) {
        // 调用服务层更新企业状态，并将结果转换为Ajax响应格式
        return toAjax(healthbrainEnterpriseService.updateStatus(Id, status));
    }

    /**
     * 删除企业管理
     *
     * @param enterpriseIds 企业管理主键串
     */
    @SaCheckPermission("healthbrain:enterprise:remove")
    @Log(title = "企业管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{enterpriseIds}")
    @Operation(summary = "删除企业管理", description = "批量删除企业管理,权限：healthbrain:enterprise:remove")
    public R<Void> remove(
            @Parameter(description = "企业管理主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] enterpriseIds) {
        return toAjax(healthbrainEnterpriseService.deleteWithValidByIds(List.of(enterpriseIds), true));
    }
    
    /**
     * 按企业类型统计
     */
    @SaCheckPermission("healthbrain:enterprise:list")
    @GetMapping("/countByType")
    @Operation(summary = "按企业类型统计", description = "按企业类型统计,权限：healthbrain:enterprise:list")
    public R<List<HealthbrainEnterpriseTypeCountVo>> countByType() {
        return R.ok(healthbrainEnterpriseService.countByType());
    }

    /**
     * 按企业发展阶段统计
     */
    @SaCheckPermission("healthbrain:enterprise:list")
    @GetMapping("/countByDevelopment")
    @Operation(summary = "按企业发展阶段统计", description = "按企业发展阶段统计,权限：healthbrain:enterprise:list")
    public R<List<HealthbrainEnterpriseTypeCountVo>> countByDevelopment() {
        return R.ok(healthbrainEnterpriseService.countByDevelopment());
    }
}