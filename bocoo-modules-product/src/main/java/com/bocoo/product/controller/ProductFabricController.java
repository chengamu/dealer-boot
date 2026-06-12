package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.FabricProfileBo;
import com.bocoo.product.domain.bo.FabricSeriesBo;
import com.bocoo.product.domain.vo.FabricProfileVo;
import com.bocoo.product.domain.vo.FabricSeriesVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductFabricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 产品面料资料接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品面料资料", description = "产品面料系列和面料资料接口")
public class ProductFabricController extends BaseController {

    private final ProductFabricService productFabricService;

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-series/list")
    @Operation(summary = "分页查询面料系列列表")
    public TableDataInfo<FabricSeriesVo> listFabricSeries(FabricSeriesBo bo, PageQuery pageQuery) {
        return productFabricService.queryFabricSeriesPage(bo, pageQuery);
    }

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-series/options")
    @Operation(summary = "查询面料系列选项")
    public R<java.util.List<FabricSeriesVo>> optionsFabricSeries(FabricSeriesBo bo) {
        return R.ok(productFabricService.queryFabricSeriesList(bo));
    }

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-series/{id}")
    @Operation(summary = "获取面料系列详情")
    public R<FabricSeriesVo> getFabricSeries(@Parameter(description = "面料系列ID", required = true) @PathVariable Long id) {
        return R.ok(productFabricService.getFabricSeriesById(id));
    }

    @SaCheckPermission("product:fabric:add")
    @Log(title = "面料系列", businessType = BusinessType.INSERT)
    @PostMapping("/fabric-series")
    @Operation(summary = "新增面料系列")
    public R<Void> addFabricSeries(@Validated @RequestBody FabricSeriesBo bo) {
        return toAjax(productFabricService.saveFabricSeries(bo));
    }

    @SaCheckPermission("product:fabric:edit")
    @Log(title = "面料系列", businessType = BusinessType.UPDATE)
    @PutMapping("/fabric-series")
    @Operation(summary = "修改面料系列")
    public R<Void> editFabricSeries(@Validated @RequestBody FabricSeriesBo bo) {
        return toAjax(productFabricService.saveFabricSeries(bo));
    }

    @SaCheckPermission("product:fabric:edit")
    @Log(title = "修改面料系列状态", businessType = BusinessType.UPDATE)
    @PutMapping("/fabric-series/change-status/{id}/{status}")
    @Operation(summary = "修改面料系列状态")
    public R<Void> changeFabricSeriesStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productFabricService.updateFabricSeriesStatus(id, status));
    }

    @SaCheckPermission("product:fabric:remove")
    @Log(title = "面料系列", businessType = BusinessType.DELETE)
    @DeleteMapping("/fabric-series/{ids}")
    @Operation(summary = "删除面料系列")
    public R<Void> removeFabricSeries(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productFabricService.removeFabricSeriesByIds(ids));
    }

    @SaCheckPermission("product:fabric:reference")
    @GetMapping("/fabric-series/{id}/references")
    @Operation(summary = "检查面料系列引用")
    public R<ReferenceCheckResultVo> checkFabricSeriesReferences(@PathVariable Long id) {
        return R.ok(productFabricService.checkFabricSeriesReferences(id));
    }

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-profiles/list")
    @Operation(summary = "分页查询面料资料列表")
    public TableDataInfo<FabricProfileVo> listFabricProfile(FabricProfileBo bo, PageQuery pageQuery) {
        return productFabricService.queryFabricProfilePage(bo, pageQuery);
    }

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-profiles/options")
    @Operation(summary = "查询面料资料选项")
    public R<java.util.List<FabricProfileVo>> optionsFabricProfile(FabricProfileBo bo) {
        return R.ok(productFabricService.queryFabricProfileList(bo));
    }

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-profiles/{id}")
    @Operation(summary = "获取面料资料详情")
    public R<FabricProfileVo> getFabricProfile(@Parameter(description = "面料资料ID", required = true) @PathVariable Long id) {
        return R.ok(productFabricService.getFabricProfileById(id));
    }

    @SaCheckPermission("product:fabric:add")
    @Log(title = "面料资料", businessType = BusinessType.INSERT)
    @PostMapping("/fabric-profiles")
    @Operation(summary = "新增面料资料")
    public R<Void> addFabricProfile(@Validated @RequestBody FabricProfileBo bo) {
        return toAjax(productFabricService.saveFabricProfile(bo));
    }

    @SaCheckPermission("product:fabric:edit")
    @Log(title = "面料资料", businessType = BusinessType.UPDATE)
    @PutMapping("/fabric-profiles")
    @Operation(summary = "修改面料资料")
    public R<Void> editFabricProfile(@Validated @RequestBody FabricProfileBo bo) {
        return toAjax(productFabricService.saveFabricProfile(bo));
    }

    @SaCheckPermission("product:fabric:edit")
    @Log(title = "修改面料资料状态", businessType = BusinessType.UPDATE)
    @PutMapping("/fabric-profiles/change-status/{id}/{status}")
    @Operation(summary = "修改面料资料状态")
    public R<Void> changeFabricProfileStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productFabricService.updateFabricProfileStatus(id, status));
    }

    @SaCheckPermission("product:fabric:remove")
    @Log(title = "面料资料", businessType = BusinessType.DELETE)
    @DeleteMapping("/fabric-profiles/{ids}")
    @Operation(summary = "删除面料资料")
    public R<Void> removeFabricProfile(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productFabricService.removeFabricProfileByIds(ids));
    }

    @SaCheckPermission("product:fabric:reference")
    @GetMapping("/fabric-profiles/{id}/references")
    @Operation(summary = "检查面料资料引用")
    public R<ReferenceCheckResultVo> checkFabricProfileReferences(@PathVariable Long id) {
        return R.ok(productFabricService.checkFabricProfileReferences(id));
    }
}
