package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.FabricSeriesBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.FabricSeriesVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.FabricSeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品面料系列接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品面料系列", description = "产品面料系列接口")
public class ProductFabricController extends BaseController {

    private final FabricSeriesService fabricSeriesService;

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-series/list")
    @Operation(summary = "分页查询面料系列列表")
    public TableDataInfo<FabricSeriesVo> listFabricSeries(FabricSeriesBo bo, PageQuery pageQuery) {
        return fabricSeriesService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-series/options")
    @Operation(summary = "查询面料系列选项")
    public R<java.util.List<FabricSeriesVo>> optionsFabricSeries(FabricSeriesBo bo) {
        return R.ok(fabricSeriesService.queryList(bo));
    }

    @SaCheckPermission("product:fabric:list")
    @GetMapping("/fabric-series/{id}")
    @Operation(summary = "获取面料系列详情")
    public R<FabricSeriesVo> getFabricSeries(@Parameter(description = "面料系列ID", required = true) @PathVariable Long id) {
        return R.ok(fabricSeriesService.queryById(id));
    }

    @SaCheckPermission("product:fabric:edit")
    @GetMapping("/fabric-series/{id}/edit-check")
    @Operation(summary = "检查面料系列是否可修改")
    public R<BaseEditCheckResultVo> checkFabricSeriesEdit(@PathVariable Long id) {
        return R.ok(fabricSeriesService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:fabric:add")
    @Log(title = "面料系列", businessType = BusinessType.INSERT)
    @PostMapping("/fabric-series")
    @Operation(summary = "新增面料系列")
    public R<Void> addFabricSeries(@Validated @RequestBody FabricSeriesBo bo) {
        return toAjax(fabricSeriesService.insertByBo(bo));
    }

    @SaCheckPermission("product:fabric:edit")
    @Log(title = "面料系列", businessType = BusinessType.UPDATE)
    @PutMapping("/fabric-series")
    @Operation(summary = "修改面料系列")
    public R<Void> editFabricSeries(@Validated @RequestBody FabricSeriesBo bo) {
        return toAjax(fabricSeriesService.updateByBo(bo));
    }

    @SaCheckPermission("product:fabric:edit")
    @Log(title = "修改面料系列状态", businessType = BusinessType.UPDATE)
    @PutMapping("/fabric-series/change-status/{id}/{status}")
    @Operation(summary = "修改面料系列状态")
    public R<Void> changeFabricSeriesStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(fabricSeriesService.updateStatus(id, status));
    }

    @SaCheckPermission("product:fabric:remove")
    @Log(title = "面料系列", businessType = BusinessType.DELETE)
    @DeleteMapping("/fabric-series/{ids}")
    @Operation(summary = "删除面料系列")
    public R<Void> removeFabricSeries(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(fabricSeriesService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:fabric:reference")
    @GetMapping("/fabric-series/{id}/references")
    @Operation(summary = "检查面料系列引用")
    public R<ReferenceCheckResultVo> checkFabricSeriesReferences(@PathVariable Long id) {
        return R.ok(fabricSeriesService.checkReferences(id));
    }
}
