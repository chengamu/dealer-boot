package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainInstrumentBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainInstrumentVo;
import com.bocoo.healthbrain.service.IHealthbrainInstrumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 医疗器械管理 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/instrument")
@Tag(name = "医疗器械管理管理", description = "医疗器械管理管理接口")
public class HealthbrainInstrumentController extends BaseController {

    private final IHealthbrainInstrumentService healthbrainInstrumentService;

    /**
     * 查询医疗器械管理列表
     */
    @SaCheckPermission("healthbrain:instrument:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询医疗器械管理列表", description = "分页查询医疗器械管理列表,权限：healthbrain:instrument:list")
    public TableDataInfo<HealthbrainInstrumentVo> list(
            @Parameter(description = "医疗器械管理查询参数")
            HealthbrainInstrumentBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainInstrumentService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询医疗器械管理列表
     */
    @SaCheckPermission("healthbrain:instrument:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询医疗器械管理列表", description = "查询医疗器械管理列表,权限：healthbrain:instrument:list")
    public List<HealthbrainInstrumentVo> alllist(
            @Parameter(description = "医疗器械管理查询参数")
            HealthbrainInstrumentBo bo) {
        return healthbrainInstrumentService.queryList(bo);
    }

    /**
     * 获取医疗器械管理详细信息
     *
     * @param instrumentId 医疗器械管理主键
     */
    @SaCheckPermission("healthbrain:instrument:query")
    @GetMapping("/{instrumentId}")
    @Operation(summary = "获取医疗器械管理详细信息", description = "根据主键获取医疗器械管理详细信息,权限：healthbrain:instrument:query")
    public R<HealthbrainInstrumentVo> getInfo(
            @Parameter(description = "医疗器械管理主键", required = true)
            @PathVariable Long instrumentId) {
        return R.ok(healthbrainInstrumentService.getVoById(instrumentId));
    }

    /**
     * 新增医疗器械管理
     */
    @SaCheckPermission("healthbrain:instrument:add")
    @Log(title = "医疗器械管理", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增医疗器械管理", description = "新增医疗器械管理,权限：healthbrain:instrument:add")
    public R<Void> add(
            @Parameter(description = "医疗器械管理信息", required = true)
            @Validated
            @RequestBody HealthbrainInstrumentBo bo) {
        return toAjax(healthbrainInstrumentService.insertByBo(bo));
    }

    /**
     * 修改医疗器械管理
     */
    @SaCheckPermission("healthbrain:instrument:edit")
    @Log(title = "医疗器械管理", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改医疗器械管理", description = "修改医疗器械管理,权限：healthbrain:instrument:edit")
    public R<Void> edit(
            @Parameter(description = "医疗器械管理信息", required = true)
            @Validated
            @RequestBody HealthbrainInstrumentBo bo) {
        return toAjax(healthbrainInstrumentService.updateByBo(bo));
    }

    /**
     * 删除医疗器械管理
     *
     * @param instrumentIds 医疗器械管理主键串
     */
    @SaCheckPermission("healthbrain:instrument:remove")
    @Log(title = "医疗器械管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{instrumentIds}")
    @Operation(summary = "删除医疗器械管理", description = "批量删除医疗器械管理,权限：healthbrain:instrument:remove")
    public R<Void> remove(
            @Parameter(description = "医疗器械管理主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] instrumentIds) {
        return toAjax(healthbrainInstrumentService.deleteWithValidByIds(List.of(instrumentIds), true));
    }

}
