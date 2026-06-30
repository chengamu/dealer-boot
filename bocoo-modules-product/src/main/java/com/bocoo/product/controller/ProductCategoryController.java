package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductCategoryVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductCategoryService;
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

import java.util.List;

/**
 * 产品分类接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability/categories")
@Tag(name = "产品分类", description = "产品分类增删改查、状态和引用检查接口")
public class ProductCategoryController extends BaseController {

    private final ProductCategoryService productCategoryService;

    @SaCheckPermission("product:base:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询产品分类列表")
    public TableDataInfo<ProductCategoryVo> list(ProductCategoryBo bo, PageQuery pageQuery) {
        return productCategoryService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/options")
    @Operation(summary = "查询产品分类选项")
    public R<List<ProductCategoryVo>> options(ProductCategoryBo bo) {
        return R.ok(productCategoryService.queryList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/tree")
    @Operation(summary = "查询产品分类树")
    public R<List<ProductCategoryVo>> tree(ProductCategoryBo bo) {
        return R.ok(productCategoryService.queryList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/{id}")
    @Operation(summary = "获取产品分类详情")
    public R<ProductCategoryVo> getById(@Parameter(description = "产品分类ID", required = true) @PathVariable Long id) {
        return R.ok(productCategoryService.queryById(id));
    }

    @SaCheckPermission("product:base:edit")
    @GetMapping("/{id}/edit-check")
    @Operation(summary = "检查产品分类是否可修改")
    public R<BaseEditCheckResultVo> checkEdit(@PathVariable Long id) {
        return R.ok(productCategoryService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:base:add")
    @Log(title = "产品分类", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增产品分类")
    public R<Void> add(@Validated @RequestBody ProductCategoryBo bo) {
        return toAjax(productCategoryService.insertByBo(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "产品分类", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改产品分类")
    public R<Void> edit(@Validated @RequestBody ProductCategoryBo bo) {
        return toAjax(productCategoryService.updateByBo(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "修改产品分类状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改产品分类状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productCategoryService.updateStatus(id, status));
    }

    @SaCheckPermission("product:base:remove")
    @Log(title = "产品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除产品分类")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productCategoryService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:base:reference")
    @GetMapping("/{id}/references")
    @Operation(summary = "检查产品分类引用")
    public R<ReferenceCheckResultVo> checkReferences(@PathVariable Long id) {
        return R.ok(productCategoryService.checkReferences(id));
    }
}
