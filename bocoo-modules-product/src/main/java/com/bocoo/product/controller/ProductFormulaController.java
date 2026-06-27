package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductFormulaBo;
import com.bocoo.product.domain.bo.ProductFormulaRejectBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductFormulaVersionVo;
import com.bocoo.product.domain.vo.ProductFormulaVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductFormulaService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

/**
 * 产品配方管理接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-formula/formulas")
@Tag(name = "产品配方管理", description = "配方主表管理接口")
public class ProductFormulaController extends BaseController {

    private final ProductFormulaService formulaService;

    @SaCheckPermission("product:formula:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询配方列表")
    public TableDataInfo<ProductFormulaVo> list(ProductFormulaBo bo, PageQuery pageQuery) {
        return formulaService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:formula:query")
    @GetMapping("/{id}")
    @Operation(summary = "获取配方详情")
    public R<ProductFormulaVo> get(@PathVariable Long id) {
        return R.ok(formulaService.queryById(id));
    }

    @SaCheckPermission("product:formula:edit")
    @GetMapping("/{id}/edit-check")
    @Operation(summary = "检查配方是否可修改")
    public R<BaseEditCheckResultVo> checkEdit(@PathVariable Long id) {
        return R.ok(formulaService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:formula:add")
    @Log(title = "配方管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增配方")
    public R<Void> add(@Validated @RequestBody ProductFormulaBo bo) {
        return toAjax(formulaService.insertByBo(bo));
    }

    @SaCheckPermission("product:formula:edit")
    @Log(title = "配方管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改配方")
    public R<Void> edit(@Validated @RequestBody ProductFormulaBo bo) {
        return toAjax(formulaService.updateByBo(bo));
    }

    @SaCheckPermission("product:formula:remove")
    @Log(title = "配方管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除配方")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(formulaService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:formula:submitReview")
    @Log(title = "提交配方审核", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/submit-review")
    @Operation(summary = "提交配方审核")
    public R<Void> submitReview(@PathVariable Long id) {
        return toAjax(formulaService.submitReview(id));
    }

    @SaCheckPermission("product:formula:approve")
    @Log(title = "审核通过配方", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过配方")
    public R<Void> approve(@PathVariable Long id) {
        return toAjax(formulaService.approve(id));
    }

    @SaCheckPermission("product:formula:reject")
    @Log(title = "驳回配方", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/reject")
    @Operation(summary = "驳回配方")
    public R<Void> reject(@PathVariable Long id, @Validated @RequestBody ProductFormulaRejectBo bo) {
        return toAjax(formulaService.reject(id, bo.getRejectReason()));
    }

    @SaCheckPermission("product:formula:stop")
    @Log(title = "停用配方", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/stop")
    @Operation(summary = "停用配方")
    public R<Void> stop(@PathVariable Long id) {
        return toAjax(formulaService.stop(id));
    }

    @SaCheckPermission("product:formula:setup")
    @Log(title = "校验配方", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/validate")
    @Operation(summary = "校验配方")
    public R<Void> validateFormula(@PathVariable Long id) {
        return toAjax(formulaService.validateFormula(id));
    }

    @SaCheckPermission("product:formula:query")
    @GetMapping("/{id}/versions")
    @Operation(summary = "查询配方版本")
    public R<List<ProductFormulaVersionVo>> versions(@PathVariable Long id) {
        return R.ok(formulaService.queryVersions(id));
    }

    @SaCheckPermission("product:formula:query")
    @GetMapping("/{id}/versions/{versionId}")
    @Operation(summary = "获取配方版本详情")
    public R<ProductFormulaVersionVo> version(@PathVariable Long id, @PathVariable Long versionId) {
        return R.ok(formulaService.queryVersionById(id, versionId));
    }

    @SaCheckPermission("product:formula:reference")
    @GetMapping("/{id}/references")
    @Operation(summary = "检查配方引用")
    public R<ReferenceCheckResultVo> references(@PathVariable Long id) {
        return R.ok(formulaService.checkReferences(id));
    }
}
