package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductManufacturerBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductManufacturerVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductManufacturerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

import java.util.List;

/**
 * 厂家管理接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability/manufacturers")
@Tag(name = "厂家管理", description = "厂家增删改查、状态、导出和引用检查接口")
public class ProductManufacturerController extends BaseController {

    private final ProductManufacturerService productManufacturerService;

    @SaCheckPermission("product:manufacturer:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询厂家列表")
    public TableDataInfo<ProductManufacturerVo> list(ProductManufacturerBo bo, PageQuery pageQuery) {
        return productManufacturerService.queryPageList(bo, pageQuery);
    }

    @Log(title = "厂家管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("product:manufacturer:export")
    @PostMapping("/export")
    @Operation(summary = "导出厂家列表")
    public void export(ProductManufacturerBo bo, HttpServletResponse response) {
        List<ProductManufacturerVo> list = productManufacturerService.queryList(bo);
        ExcelUtil.exportExcel(list, "厂家数据", ProductManufacturerVo.class, response);
    }

    @SaCheckPermission("product:manufacturer:list")
    @GetMapping("/options")
    @Operation(summary = "查询厂家选项")
    public R<List<ProductManufacturerVo>> options(ProductManufacturerBo bo) {
        return R.ok(productManufacturerService.queryList(bo));
    }

    @SaCheckPermission("product:manufacturer:list")
    @GetMapping("/{id}")
    @Operation(summary = "获取厂家详情")
    public R<ProductManufacturerVo> getById(@PathVariable Long id) {
        return R.ok(productManufacturerService.queryById(id));
    }

    @SaCheckPermission("product:manufacturer:edit")
    @GetMapping("/{id}/edit-check")
    @Operation(summary = "检查厂家是否可修改")
    public R<BaseEditCheckResultVo> checkEdit(@PathVariable Long id) {
        return R.ok(productManufacturerService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:manufacturer:add")
    @Log(title = "厂家管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增厂家")
    public R<Void> add(@Validated @RequestBody ProductManufacturerBo bo) {
        return toAjax(productManufacturerService.insertByBo(bo));
    }

    @SaCheckPermission("product:manufacturer:edit")
    @Log(title = "厂家管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改厂家")
    public R<Void> edit(@Validated @RequestBody ProductManufacturerBo bo) {
        return toAjax(productManufacturerService.updateByBo(bo));
    }

    @SaCheckPermission("product:manufacturer:changeStatus")
    @Log(title = "修改厂家状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改厂家状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productManufacturerService.updateStatus(id, status));
    }

    @SaCheckPermission("product:manufacturer:remove")
    @Log(title = "厂家管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除厂家")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productManufacturerService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:manufacturer:reference")
    @GetMapping("/{id}/references")
    @Operation(summary = "检查厂家引用")
    public R<ReferenceCheckResultVo> checkReferences(@PathVariable Long id) {
        return R.ok(productManufacturerService.checkReferences(id));
    }
}
