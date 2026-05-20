package com.bocoo.healthbrain.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.healthbrain.domain.bo.HealthbrainFundTypeBo;
import com.bocoo.healthbrain.domain.vo.HealthbrainFundTypeVo;
import com.bocoo.healthbrain.service.IHealthbrainFundTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 资金类型 控制层
 *
 * @author cmx
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/healthbrain/fundType")
@Tag(name = "资金类型管理", description = "资金类型管理接口")
public class HealthbrainFundTypeController extends BaseController {

    private final IHealthbrainFundTypeService healthbrainFundTypeService;

    /**
     * 查询资金类型列表
     */
    @SaCheckPermission("healthbrain:fundType:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询资金类型列表", description = "分页查询资金类型列表,权限：healthbrain:fundType:list")
    public TableDataInfo<HealthbrainFundTypeVo> list(
            @Parameter(description = "资金类型查询参数")
            HealthbrainFundTypeBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return healthbrainFundTypeService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询资金类型列表
     */
    @SaCheckPermission("healthbrain:fundType:list")
    @GetMapping("/alllist")
    @Operation(summary = "查询资金类型列表", description = "查询资金类型列表,权限：healthbrain:fundType:list")
    public List<HealthbrainFundTypeVo> alllist(
            @Parameter(description = "资金类型查询参数")
            HealthbrainFundTypeBo bo) {
        return healthbrainFundTypeService.queryList(bo);
    }

    /**
     * 获取资金类型详细信息
     *
     * @param fundTypeId 资金类型主键
     */
    @SaCheckPermission("healthbrain:fundType:query")
    @GetMapping("/{fundTypeId}")
    @Operation(summary = "获取资金类型详细信息", description = "根据主键获取资金类型详细信息,权限：healthbrain:fundType:query")
    public R<HealthbrainFundTypeVo> getInfo(
            @Parameter(description = "资金类型主键", required = true)
            @PathVariable Long fundTypeId) {
        return R.ok(healthbrainFundTypeService.getVoById(fundTypeId));
    }

    /**
     * 新增资金类型
     */
    @SaCheckPermission("healthbrain:fundType:add")
    @Log(title = "资金类型", businessType = BusinessType.INSERT)
    @PostMapping()
    @Operation(summary = "新增资金类型", description = "新增资金类型,权限：healthbrain:fundType:add")
    public R<Void> add(
            @Parameter(description = "资金类型信息", required = true)
            @Validated
            @RequestBody HealthbrainFundTypeBo bo) {
        return toAjax(healthbrainFundTypeService.insertByBo(bo));
    }

    /**
     * 修改资金类型
     */
    @SaCheckPermission("healthbrain:fundType:edit")
    @Log(title = "资金类型", businessType = BusinessType.UPDATE)
    @PutMapping()
    @Operation(summary = "修改资金类型", description = "修改资金类型,权限：healthbrain:fundType:edit")
    public R<Void> edit(
            @Parameter(description = "资金类型信息", required = true)
            @Validated
            @RequestBody HealthbrainFundTypeBo bo) {
        return toAjax(healthbrainFundTypeService.updateByBo(bo));
    }

    /**
     * 删除资金类型
     *
     * @param fundTypeIds 资金类型主键串
     */
    @SaCheckPermission("healthbrain:fundType:remove")
    @Log(title = "资金类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{fundTypeIds}")
    @Operation(summary = "删除资金类型", description = "批量删除资金类型,权限：healthbrain:fundType:remove")
    public R<Void> remove(
            @Parameter(description = "资金类型主键数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] fundTypeIds) {
        return toAjax(healthbrainFundTypeService.deleteWithValidByIds(List.of(fundTypeIds), true));
    }

}
