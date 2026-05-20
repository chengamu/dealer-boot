package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainFundDataBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainFundDataVo;
import com.bocoo.healthbrain.service.IHealthbrainFundDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 资金数据 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/fundData")
@Tag(name = "资金数据管理", description = "资金数据管理接口")
public class HealthbrainFundDataController extends BaseController {

    private final IHealthbrainFundDataService healthbrainFundDataService;

    /**
     * 查询资金数据列表
     */
    @SaCheckPermission("healthbrain:fundData:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询资金数据列表", description = "分页查询资金数据列表,权限：healthbrain:fundData:list")
    public TableDataInfo<HealthbrainFundDataVo> list(
            @Parameter(description = "资金数据查询参数")
            HealthbrainFundDataBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainFundDataService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询资金数据列表
     */
    @SaCheckPermission("healthbrain:fundData:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询资金数据列表", description = "查询资金数据列表,权限：healthbrain:fundData:list")
    public List<HealthbrainFundDataVo> alllist(
            @Parameter(description = "资金数据查询参数")
            HealthbrainFundDataBo bo) {
        return healthbrainFundDataService.queryList(bo);
    }

    /**
     * 获取资金数据详细信息
     *
     * @param fundDataId 资金数据主键
     */
    @SaCheckPermission("healthbrain:fundData:query")
    @GetMapping("/{fundDataId}")
    @Operation(summary = "获取资金数据详细信息", description = "根据主键获取资金数据详细信息,权限：healthbrain:fundData:query")
    public R<HealthbrainFundDataVo> getInfo(
            @Parameter(description = "资金数据主键", required = true)
            @PathVariable Long fundDataId) {
        return R.ok(healthbrainFundDataService.getVoById(fundDataId));
    }

    /**
     * 新增资金数据
     */
    @SaCheckPermission("healthbrain:fundData:add")
    @Log(title = "资金数据", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增资金数据", description = "新增资金数据,权限：healthbrain:fundData:add")
    public R<Void> add(
            @Parameter(description = "资金数据信息", required = true)
            @Validated
            @RequestBody HealthbrainFundDataBo bo) {
        return toAjax(healthbrainFundDataService.insertByBo(bo));
    }

    /**
     * 修改资金数据
     */
    @SaCheckPermission("healthbrain:fundData:edit")
    @Log(title = "资金数据", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改资金数据", description = "修改资金数据,权限：healthbrain:fundData:edit")
    public R<Void> edit(
            @Parameter(description = "资金数据信息", required = true)
            @Validated
            @RequestBody HealthbrainFundDataBo bo) {
        return toAjax(healthbrainFundDataService.updateByBo(bo));
    }

    /**
     * 删除资金数据
     *
     * @param fundDataIds 资金数据主键串
     */
    @SaCheckPermission("healthbrain:fundData:remove")
    @Log(title = "资金数据", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fundDataIds}")
    @Operation(summary = "删除资金数据", description = "批量删除资金数据,权限：healthbrain:fundData:remove")
    public R<Void> remove(
            @Parameter(description = "资金数据主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] fundDataIds) {
        return toAjax(healthbrainFundDataService.deleteWithValidByIds(List.of(fundDataIds), true));
    }

}
