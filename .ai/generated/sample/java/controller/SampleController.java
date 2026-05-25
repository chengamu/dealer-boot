package com.bocoo.demo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.demo.domain.bo.SampleBo;
import com.bocoo.demo.domain.vo.SampleVo;
import com.bocoo.demo.service.ISampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 样本 控制层
 *
 * @author validator
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/sample")
@Tag(name = "样本管理", description = "样本管理接口")
public class SampleController extends BaseController {

    private final ISampleService sampleService;

    /**
     * 查询样本列表
     */
    @SaCheckPermission("demo:sample:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询样本列表", description = "分页查询样本列表,权限：demo:sample:list")
    public TableDataInfo<SampleVo> list(
            @Parameter(description = "样本查询参数")
            SampleBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return sampleService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询样本列表
     */
    @SaCheckPermission("demo:sample:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询样本列表", description = "查询样本列表,权限：demo:sample:list")
    public List<SampleVo> alllist(
            @Parameter(description = "样本查询参数")
            SampleBo bo) {
        return sampleService.queryList(bo);
    }

    /**
     * 获取样本详细信息
     *
     * @param sampleId 样本主键
     */
    @SaCheckPermission("demo:sample:query")
    @GetMapping("/{sampleId}")
    @Operation(summary = "获取样本详细信息", description = "根据主键获取样本详细信息,权限：demo:sample:query")
    public R<SampleVo> getInfo(
            @Parameter(description = "样本主键", required = true)
            @PathVariable Long sampleId) {
        return R.ok(sampleService.getVoById(sampleId));
    }

    /**
     * 新增样本
     */
    @SaCheckPermission("demo:sample:add")
    @Log(title = "样本", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增样本", description = "新增样本,权限：demo:sample:add")
    public R<Void> add(
            @Parameter(description = "样本信息", required = true)
            @Validated
            @RequestBody SampleBo bo) {
        return toAjax(sampleService.insertByBo(bo));
    }

    /**
     * 修改样本
     */
    @SaCheckPermission("demo:sample:edit")
    @Log(title = "样本", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改样本", description = "修改样本,权限：demo:sample:edit")
    public R<Void> edit(
            @Parameter(description = "样本信息", required = true)
            @Validated
            @RequestBody SampleBo bo) {
        return toAjax(sampleService.updateByBo(bo));
    }

    /**
     * 删除样本
     *
     * @param sampleIds 样本主键串
     */
    @SaCheckPermission("demo:sample:remove")
    @Log(title = "样本", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sampleIds}")
    @Operation(summary = "删除样本", description = "批量删除样本,权限：demo:sample:remove")
    public R<Void> remove(
            @Parameter(description = "样本主键数组", required = true)
            @NotEmpty(message = "{gen.validation.pk.required}")
            @PathVariable Long[] sampleIds) {
        return toAjax(sampleService.deleteWithValidByIds(List.of(sampleIds), true));
    }

    /**
     * 修改样本状态
     */
    @SaCheckPermission("demo:sample:edit")
    @Log(title = "修改样本状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus/{sampleId}/{status}")
    @Operation(summary = "修改样本状态", description = "修改样本状态,权限：demo:sample:edit")
    public R<Void> changeStatus(
            @Parameter(description = "样本主键", required = true)
            @PathVariable Long sampleId,
            @Parameter(description = "样本状态", required = true)
            @PathVariable String status) {
        return toAjax(sampleService.updateStatus(sampleId, status));
    }
}
