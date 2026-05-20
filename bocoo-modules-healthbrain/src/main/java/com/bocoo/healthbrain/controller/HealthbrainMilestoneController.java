package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainMilestoneBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainMilestoneVo;
import com.bocoo.healthbrain.service.IHealthbrainMilestoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 里程碑数据 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/milestone")
@Tag(name = "里程碑数据管理", description = "里程碑数据管理接口")
public class HealthbrainMilestoneController extends BaseController {

    private final IHealthbrainMilestoneService healthbrainMilestoneService;

    /**
     * 查询里程碑数据列表
     */
    @SaCheckPermission("healthbrain:milestone:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询里程碑数据列表", description = "分页查询里程碑数据列表,权限：healthbrain:milestone:list")
    public TableDataInfo<HealthbrainMilestoneVo> list(
            @Parameter(description = "里程碑数据查询参数")
            HealthbrainMilestoneBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainMilestoneService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询里程碑数据列表
     */
    @SaCheckPermission("healthbrain:milestone:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询里程碑数据列表", description = "查询里程碑数据列表,权限：healthbrain:milestone:list")
    public List<HealthbrainMilestoneVo> alllist(
            @Parameter(description = "里程碑数据查询参数")
            HealthbrainMilestoneBo bo) {
        return healthbrainMilestoneService.queryList(bo);
    }

    /**
     * 获取里程碑数据详细信息
     *
     * @param milestoneId 里程碑数据主键
     */
    @SaCheckPermission("healthbrain:milestone:query")
    @GetMapping("/{milestoneId}")
    @Operation(summary = "获取里程碑数据详细信息", description = "根据主键获取里程碑数据详细信息,权限：healthbrain:milestone:query")
    public R<HealthbrainMilestoneVo> getInfo(
            @Parameter(description = "里程碑数据主键", required = true)
            @PathVariable Long milestoneId) {
        return R.ok(healthbrainMilestoneService.getVoById(milestoneId));
    }

    /**
     * 新增里程碑数据
     */
    @SaCheckPermission("healthbrain:milestone:add")
    @Log(title = "里程碑数据", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增里程碑数据", description = "新增里程碑数据,权限：healthbrain:milestone:add")
    public R<Void> add(
            @Parameter(description = "里程碑数据信息", required = true)
            @Validated
            @RequestBody HealthbrainMilestoneBo bo) {
        return toAjax(healthbrainMilestoneService.insertByBo(bo));
    }

    /**
     * 修改里程碑数据
     */
    @SaCheckPermission("healthbrain:milestone:edit")
    @Log(title = "里程碑数据", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改里程碑数据", description = "修改里程碑数据,权限：healthbrain:milestone:edit")
    public R<Void> edit(
            @Parameter(description = "里程碑数据信息", required = true)
            @Validated
            @RequestBody HealthbrainMilestoneBo bo) {
        return toAjax(healthbrainMilestoneService.updateByBo(bo));
    }


    /**
     * 修改里程碑状态
     *
     * @param Id 里程碑ID
     * @param status 状态值
     * @return 操作结果，成功返回R.success()，失败返回R.error()
     */
    @SaCheckPermission("healthbrain:milestone:edit")
    @Log(title = "修改里程碑状态", businessType = BusinessType.UPDATE)
    @PostMapping("/changeStatus/{Id}/{status}")
    @Operation(summary = "修改里程碑状态", description = "修改里程碑状态,权限：healthbrain:milestone:edit")
    public R<Void> changeStatus(
            @Parameter(description = "ID", required = true)
            @PathVariable Long Id,
            @Parameter(description = "状态", required = true)
            @PathVariable String status) {
        // 调用服务层更新里程碑状态，并将结果转换为Ajax响应格式
        return toAjax(healthbrainMilestoneService.updateStatus(Id, status));
    }


    /**
     * 删除里程碑数据
     *
     * @param milestoneIds 里程碑数据主键串
     */
    @SaCheckPermission("healthbrain:milestone:remove")
    @Log(title = "里程碑数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{milestoneIds}")
    @Operation(summary = "删除里程碑数据", description = "批量删除里程碑数据,权限：healthbrain:milestone:remove")
    public R<Void> remove(
            @Parameter(description = "里程碑数据主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] milestoneIds) {
        return toAjax(healthbrainMilestoneService.deleteWithValidByIds(List.of(milestoneIds), true));
    }

}
