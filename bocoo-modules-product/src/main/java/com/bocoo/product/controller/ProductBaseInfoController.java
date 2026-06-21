package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductBaseAttributeBo;
import com.bocoo.product.domain.bo.ProductCategoryBo;
import com.bocoo.product.domain.bo.ProductUnitBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductBaseAttributeVo;
import com.bocoo.product.domain.vo.ProductCategoryVo;
import com.bocoo.product.domain.vo.ProductUnitVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductBaseAttributeService;
import com.bocoo.product.service.ProductCategoryService;
import com.bocoo.product.service.ProductUnitService;
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
 * 产品基础资料：分类、单位、配置字典。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品基础资料", description = "产品分类、单位和配置字典接口")
public class ProductBaseInfoController extends BaseController {

    private final ProductCategoryService productCategoryService;
    private final ProductUnitService productUnitService;
    private final ProductBaseAttributeService productBaseAttributeService;

    @SaCheckPermission("product:base:list")
    @GetMapping("/categories/list")
    @Operation(summary = "分页查询产品分类列表")
    public TableDataInfo<ProductCategoryVo> listProductCategory(ProductCategoryBo bo, PageQuery pageQuery) {
        return productCategoryService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/categories/options")
    @Operation(summary = "查询产品分类选项")
    public R<java.util.List<ProductCategoryVo>> optionsProductCategory(ProductCategoryBo bo) {
        return R.ok(productCategoryService.queryList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/categories/tree")
    @Operation(summary = "查询产品分类树")
    public R<java.util.List<ProductCategoryVo>> treeProductCategory(ProductCategoryBo bo) {
        return R.ok(productCategoryService.queryList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/categories/{id}")
    @Operation(summary = "获取产品分类详情")
    public R<ProductCategoryVo> getProductCategory(@Parameter(description = "产品分类ID", required = true) @PathVariable Long id) {
        return R.ok(productCategoryService.queryById(id));
    }

    @SaCheckPermission("product:base:edit")
    @GetMapping("/categories/{id}/edit-check")
    @Operation(summary = "检查产品分类是否可修改")
    public R<BaseEditCheckResultVo> checkProductCategoryEdit(@PathVariable Long id) {
        return R.ok(productCategoryService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:base:add")
    @Log(title = "产品分类", businessType = BusinessType.INSERT)
    @PostMapping("/categories")
    @Operation(summary = "新增产品分类")
    public R<Void> addProductCategory(@Validated @RequestBody ProductCategoryBo bo) {
        return toAjax(productCategoryService.insertByBo(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "产品分类", businessType = BusinessType.UPDATE)
    @PutMapping("/categories")
    @Operation(summary = "修改产品分类")
    public R<Void> editProductCategory(@Validated @RequestBody ProductCategoryBo bo) {
        return toAjax(productCategoryService.updateByBo(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "修改产品分类状态", businessType = BusinessType.UPDATE)
    @PutMapping("/categories/change-status/{id}/{status}")
    @Operation(summary = "修改产品分类状态")
    public R<Void> changeProductCategoryStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productCategoryService.updateStatus(id, status));
    }

    @SaCheckPermission("product:base:remove")
    @Log(title = "产品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/categories/{ids}")
    @Operation(summary = "删除产品分类")
    public R<Void> removeProductCategory(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productCategoryService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:base:reference")
    @GetMapping("/categories/{id}/references")
    @Operation(summary = "检查产品分类引用")
    public R<ReferenceCheckResultVo> checkProductCategoryReferences(@PathVariable Long id) {
        return R.ok(productCategoryService.checkReferences(id));
    }

    @SaCheckPermission("product:unit:list")
    @GetMapping("/units/list")
    @Operation(summary = "分页查询产品单位列表")
    public TableDataInfo<ProductUnitVo> listProductUnit(ProductUnitBo bo, PageQuery pageQuery) {
        return productUnitService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:unit:list")
    @GetMapping("/units/options")
    @Operation(summary = "查询产品单位选项")
    public R<java.util.List<ProductUnitVo>> optionsProductUnit(ProductUnitBo bo) {
        return R.ok(productUnitService.queryList(bo));
    }

    @SaCheckPermission("product:unit:list")
    @GetMapping("/units/{id}")
    @Operation(summary = "获取产品单位详情")
    public R<ProductUnitVo> getProductUnit(@Parameter(description = "产品单位ID", required = true) @PathVariable Long id) {
        return R.ok(productUnitService.queryById(id));
    }

    @SaCheckPermission("product:unit:edit")
    @GetMapping("/units/{id}/edit-check")
    @Operation(summary = "检查产品单位是否可修改")
    public R<BaseEditCheckResultVo> checkProductUnitEdit(@PathVariable Long id) {
        return R.ok(productUnitService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:unit:add")
    @Log(title = "产品单位", businessType = BusinessType.INSERT)
    @PostMapping("/units")
    @Operation(summary = "新增产品单位")
    public R<Void> addProductUnit(@Validated @RequestBody ProductUnitBo bo) {
        return toAjax(productUnitService.insertByBo(bo));
    }

    @SaCheckPermission("product:unit:edit")
    @Log(title = "产品单位", businessType = BusinessType.UPDATE)
    @PutMapping("/units")
    @Operation(summary = "修改产品单位")
    public R<Void> editProductUnit(@Validated @RequestBody ProductUnitBo bo) {
        return toAjax(productUnitService.updateByBo(bo));
    }

    @SaCheckPermission("product:unit:edit")
    @Log(title = "修改产品单位状态", businessType = BusinessType.UPDATE)
    @PutMapping("/units/change-status/{id}/{status}")
    @Operation(summary = "修改产品单位状态")
    public R<Void> changeProductUnitStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productUnitService.updateStatus(id, status));
    }

    @SaCheckPermission("product:unit:remove")
    @Log(title = "产品单位", businessType = BusinessType.DELETE)
    @DeleteMapping("/units/{ids}")
    @Operation(summary = "删除产品单位")
    public R<Void> removeProductUnit(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productUnitService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:unit:reference")
    @GetMapping("/units/{id}/references")
    @Operation(summary = "检查产品单位引用")
    public R<ReferenceCheckResultVo> checkProductUnitReferences(@PathVariable Long id) {
        return R.ok(productUnitService.checkReferences(id));
    }

    @SaCheckPermission("product:base-attribute:list")
    @GetMapping("/base-attributes/list")
    @Operation(summary = "分页查询基础属性列表")
    public TableDataInfo<ProductBaseAttributeVo> listProductBaseAttribute(ProductBaseAttributeBo bo, PageQuery pageQuery) {
        return productBaseAttributeService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:base-attribute:list")
    @GetMapping("/base-attributes/options")
    @Operation(summary = "查询基础属性选项")
    public R<java.util.List<ProductBaseAttributeVo>> optionsProductBaseAttribute(ProductBaseAttributeBo bo) {
        return R.ok(productBaseAttributeService.queryList(bo));
    }

    @SaCheckPermission("product:base-attribute:list")
    @GetMapping("/base-attributes/{id}")
    @Operation(summary = "获取基础属性详情")
    public R<ProductBaseAttributeVo> getProductBaseAttribute(@Parameter(description = "基础属性ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseAttributeService.queryById(id));
    }

    @SaCheckPermission("product:base-attribute:edit")
    @GetMapping("/base-attributes/{id}/edit-check")
    @Operation(summary = "检查基础属性是否可修改")
    public R<BaseEditCheckResultVo> checkProductBaseAttributeEdit(@PathVariable Long id) {
        return R.ok(productBaseAttributeService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:base-attribute:add")
    @Log(title = "基础属性", businessType = BusinessType.INSERT)
    @PostMapping("/base-attributes")
    @Operation(summary = "新增基础属性")
    public R<Void> addProductBaseAttribute(@Validated @RequestBody ProductBaseAttributeBo bo) {
        return toAjax(productBaseAttributeService.insertByBo(bo));
    }

    @SaCheckPermission("product:base-attribute:edit")
    @Log(title = "基础属性", businessType = BusinessType.UPDATE)
    @PutMapping("/base-attributes")
    @Operation(summary = "修改基础属性")
    public R<Void> editProductBaseAttribute(@Validated @RequestBody ProductBaseAttributeBo bo) {
        return toAjax(productBaseAttributeService.updateByBo(bo));
    }

    @SaCheckPermission("product:base-attribute:edit")
    @Log(title = "修改基础属性状态", businessType = BusinessType.UPDATE)
    @PutMapping("/base-attributes/change-status/{id}/{status}")
    @Operation(summary = "修改基础属性状态")
    public R<Void> changeProductBaseAttributeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseAttributeService.updateStatus(id, status));
    }

    @SaCheckPermission("product:base-attribute:remove")
    @Log(title = "基础属性", businessType = BusinessType.DELETE)
    @DeleteMapping("/base-attributes/{ids}")
    @Operation(summary = "删除基础属性")
    public R<Void> removeProductBaseAttribute(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseAttributeService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:base-attribute:reference")
    @GetMapping("/base-attributes/{id}/references")
    @Operation(summary = "检查基础属性引用")
    public R<ReferenceCheckResultVo> checkProductBaseAttributeReferences(@PathVariable Long id) {
        return R.ok(productBaseAttributeService.checkReferences(id));
    }
}
