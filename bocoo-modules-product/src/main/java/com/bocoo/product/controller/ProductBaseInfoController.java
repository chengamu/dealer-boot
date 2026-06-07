package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.*;
import com.bocoo.product.domain.vo.*;
import com.bocoo.product.service.ProductBaseInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 产品能力基础信息接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品能力基础信息", description = "产品能力基础信息接口")
public class ProductBaseInfoController extends BaseController {

    private final ProductBaseInfoService productBaseInfoService;

    @SaCheckPermission("product:center:view")
    @GetMapping("/workbench/summary")
    @Operation(summary = "查询产品能力工作台汇总")
    public R<WorkbenchSummaryVo> getWorkbenchSummary() {
        return R.ok(productBaseInfoService.getWorkbenchSummary());
    }

    @SaCheckPermission("product:center:view")
    @GetMapping("/workbench/progress/list")
    @Operation(summary = "分页查询产品能力配置进度")
    public TableDataInfo<WorkbenchProgressVo> listWorkbenchProgress(PageQuery pageQuery) {
        return productBaseInfoService.queryWorkbenchProgressPage();
    }

    @SaCheckPermission("product:center:view")
    @GetMapping("/workbench/priority/list")
    @Operation(summary = "分页查询产品能力优先队列")
    public TableDataInfo<WorkbenchPriorityVo> listWorkbenchPriority(PageQuery pageQuery) {
        return productBaseInfoService.queryWorkbenchPriorityPage();
    }

    @SaCheckPermission("product:center:view")
    @GetMapping("/workbench/sync-events")
    @Operation(summary = "分页查询产品能力同步事件")
    public TableDataInfo<WorkbenchSyncEventVo> listWorkbenchSyncEvent(PageQuery pageQuery) {
        return productBaseInfoService.queryWorkbenchSyncEventPage();
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/categories/list")
    @Operation(summary = "分页查询产品分类列表")
    public TableDataInfo<ProductCategoryVo> listProductCategory(ProductCategoryBo bo, PageQuery pageQuery) {
        return productBaseInfoService.queryProductCategoryPage(bo, pageQuery);
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/categories/options")
    @Operation(summary = "查询产品分类选项")
    public R<java.util.List<ProductCategoryVo>> optionsProductCategory(ProductCategoryBo bo) {
        return R.ok(productBaseInfoService.queryProductCategoryList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/categories/tree")
    @Operation(summary = "查询产品分类树")
    public R<java.util.List<ProductCategoryVo>> treeProductCategory(ProductCategoryBo bo) {
        return R.ok(productBaseInfoService.queryProductCategoryList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/categories/{id}")
    @Operation(summary = "获取产品分类详情")
    public R<ProductCategoryVo> getProductCategory(@Parameter(description = "产品分类ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseInfoService.getProductCategoryById(id));
    }

    @SaCheckPermission("product:base:add")
    @Log(title = "产品分类", businessType = BusinessType.INSERT)
    @PostMapping("/categories")
    @Operation(summary = "新增产品分类")
    public R<Void> addProductCategory(@Validated @RequestBody ProductCategoryBo bo) {
        return toAjax(productBaseInfoService.saveProductCategory(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "产品分类", businessType = BusinessType.UPDATE)
    @PutMapping("/categories")
    @Operation(summary = "修改产品分类")
    public R<Void> editProductCategory(@Validated @RequestBody ProductCategoryBo bo) {
        return toAjax(productBaseInfoService.saveProductCategory(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "修改产品分类状态", businessType = BusinessType.UPDATE)
    @PutMapping("/categories/change-status/{id}/{status}")
    @Operation(summary = "修改产品分类状态")
    public R<Void> changeProductCategoryStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseInfoService.updateProductCategoryStatus(id, status));
    }

    @SaCheckPermission("product:base:remove")
    @Log(title = "产品分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/categories/{ids}")
    @Operation(summary = "删除产品分类")
    public R<Void> removeProductCategory(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseInfoService.removeProductCategoryByIds(ids));
    }

    @SaCheckPermission("product:base:reference")
    @GetMapping("/categories/{id}/references")
    @Operation(summary = "检查产品分类引用")
    public R<ReferenceCheckResultVo> checkProductCategoryReferences(@PathVariable Long id) {
        return R.ok(productBaseInfoService.checkProductCategoryReferences(id));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/list")
    @Operation(summary = "分页查询产品物料列表")
    public TableDataInfo<ProductMaterialVo> listProductMaterial(ProductMaterialBo bo, PageQuery pageQuery) {
        return productBaseInfoService.queryProductMaterialPage(bo, pageQuery);
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/options")
    @Operation(summary = "查询产品物料选项")
    public R<java.util.List<ProductMaterialVo>> optionsProductMaterial(ProductMaterialBo bo) {
        return R.ok(productBaseInfoService.queryProductMaterialList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/{id}")
    @Operation(summary = "获取产品物料详情")
    public R<ProductMaterialVo> getProductMaterial(@Parameter(description = "产品物料ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseInfoService.getProductMaterialById(id));
    }

    @SaCheckPermission("product:base:add")
    @Log(title = "产品物料", businessType = BusinessType.INSERT)
    @PostMapping("/materials")
    @Operation(summary = "新增产品物料")
    public R<Void> addProductMaterial(@Validated @RequestBody ProductMaterialBo bo) {
        return toAjax(productBaseInfoService.saveProductMaterial(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "产品物料", businessType = BusinessType.UPDATE)
    @PutMapping("/materials")
    @Operation(summary = "修改产品物料")
    public R<Void> editProductMaterial(@Validated @RequestBody ProductMaterialBo bo) {
        return toAjax(productBaseInfoService.saveProductMaterial(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "修改产品物料状态", businessType = BusinessType.UPDATE)
    @PutMapping("/materials/change-status/{id}/{status}")
    @Operation(summary = "修改产品物料状态")
    public R<Void> changeProductMaterialStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseInfoService.updateProductMaterialStatus(id, status));
    }

    @SaCheckPermission("product:base:remove")
    @Log(title = "产品物料", businessType = BusinessType.DELETE)
    @DeleteMapping("/materials/{ids}")
    @Operation(summary = "删除产品物料")
    public R<Void> removeProductMaterial(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseInfoService.removeProductMaterialByIds(ids));
    }

    @SaCheckPermission("product:base:reference")
    @GetMapping("/materials/{id}/references")
    @Operation(summary = "检查产品物料引用")
    public R<ReferenceCheckResultVo> checkProductMaterialReferences(@PathVariable Long id) {
        return R.ok(productBaseInfoService.checkProductMaterialReferences(id));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/components/list")
    @Operation(summary = "分页查询产品组件列表")
    public TableDataInfo<ProductComponentVo> listProductComponent(ProductComponentBo bo, PageQuery pageQuery) {
        return productBaseInfoService.queryProductComponentPage(bo, pageQuery);
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/components/options")
    @Operation(summary = "查询产品组件选项")
    public R<java.util.List<ProductComponentVo>> optionsProductComponent(ProductComponentBo bo) {
        return R.ok(productBaseInfoService.queryProductComponentList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/components/{id}")
    @Operation(summary = "获取产品组件详情")
    public R<ProductComponentVo> getProductComponent(@Parameter(description = "产品组件ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseInfoService.getProductComponentById(id));
    }

    @SaCheckPermission("product:base:add")
    @Log(title = "产品组件", businessType = BusinessType.INSERT)
    @PostMapping("/components")
    @Operation(summary = "新增产品组件")
    public R<Void> addProductComponent(@Validated @RequestBody ProductComponentBo bo) {
        return toAjax(productBaseInfoService.saveProductComponent(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "产品组件", businessType = BusinessType.UPDATE)
    @PutMapping("/components")
    @Operation(summary = "修改产品组件")
    public R<Void> editProductComponent(@Validated @RequestBody ProductComponentBo bo) {
        return toAjax(productBaseInfoService.saveProductComponent(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "修改产品组件状态", businessType = BusinessType.UPDATE)
    @PutMapping("/components/change-status/{id}/{status}")
    @Operation(summary = "修改产品组件状态")
    public R<Void> changeProductComponentStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseInfoService.updateProductComponentStatus(id, status));
    }

    @SaCheckPermission("product:base:remove")
    @Log(title = "产品组件", businessType = BusinessType.DELETE)
    @DeleteMapping("/components/{ids}")
    @Operation(summary = "删除产品组件")
    public R<Void> removeProductComponent(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseInfoService.removeProductComponentByIds(ids));
    }

    @SaCheckPermission("product:base:reference")
    @GetMapping("/components/{id}/references")
    @Operation(summary = "检查产品组件引用")
    public R<ReferenceCheckResultVo> checkProductComponentReferences(@PathVariable Long id) {
        return R.ok(productBaseInfoService.checkProductComponentReferences(id));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/list")
    @Operation(summary = "分页查询产品资料资产列表")
    public TableDataInfo<ProductMediaAssetVo> listProductMediaAsset(ProductMediaAssetBo bo, PageQuery pageQuery) {
        return productBaseInfoService.queryProductMediaAssetPage(bo, pageQuery);
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/options")
    @Operation(summary = "查询产品资料资产选项")
    public R<java.util.List<ProductMediaAssetVo>> optionsProductMediaAsset(ProductMediaAssetBo bo) {
        return R.ok(productBaseInfoService.queryProductMediaAssetList(bo));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/{id}")
    @Operation(summary = "获取产品资料资产详情")
    public R<ProductMediaAssetVo> getProductMediaAsset(@Parameter(description = "产品资料资产ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseInfoService.getProductMediaAssetById(id));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "产品资料资产", businessType = BusinessType.INSERT)
    @PostMapping("/media-assets")
    @Operation(summary = "新增产品资料资产")
    public R<Void> addProductMediaAsset(@Validated @RequestBody ProductMediaAssetBo bo) {
        return toAjax(productBaseInfoService.saveProductMediaAsset(bo));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "产品资料资产", businessType = BusinessType.UPDATE)
    @PutMapping("/media-assets")
    @Operation(summary = "修改产品资料资产")
    public R<Void> editProductMediaAsset(@Validated @RequestBody ProductMediaAssetBo bo) {
        return toAjax(productBaseInfoService.saveProductMediaAsset(bo));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "修改产品资料资产状态", businessType = BusinessType.UPDATE)
    @PutMapping("/media-assets/change-status/{id}/{status}")
    @Operation(summary = "修改产品资料资产状态")
    public R<Void> changeProductMediaAssetStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseInfoService.updateProductMediaAssetStatus(id, status));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "产品资料资产", businessType = BusinessType.DELETE)
    @DeleteMapping("/media-assets/{ids}")
    @Operation(summary = "删除产品资料资产")
    public R<Void> removeProductMediaAsset(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseInfoService.removeProductMediaAssetByIds(ids));
    }

    @SaCheckPermission("product:asset:reference")
    @GetMapping("/media-assets/{id}/references")
    @Operation(summary = "检查产品资料资产引用")
    public R<ReferenceCheckResultVo> checkProductMediaAssetReferences(@PathVariable Long id) {
        return R.ok(productBaseInfoService.checkProductMediaAssetReferences(id));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/list")
    @Operation(summary = "分页查询产品资料绑定列表")
    public TableDataInfo<ProductMediaBindingVo> listProductMediaBinding(ProductMediaBindingBo bo, PageQuery pageQuery) {
        return productBaseInfoService.queryProductMediaBindingPage(bo, pageQuery);
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/options")
    @Operation(summary = "查询产品资料绑定选项")
    public R<java.util.List<ProductMediaBindingVo>> optionsProductMediaBinding(ProductMediaBindingBo bo) {
        return R.ok(productBaseInfoService.queryProductMediaBindingList(bo));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/{id}")
    @Operation(summary = "获取产品资料绑定详情")
    public R<ProductMediaBindingVo> getProductMediaBinding(@Parameter(description = "产品资料绑定ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseInfoService.getProductMediaBindingById(id));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料绑定", businessType = BusinessType.INSERT)
    @PostMapping("/media-bindings")
    @Operation(summary = "新增产品资料绑定")
    public R<Void> addProductMediaBinding(@Validated @RequestBody ProductMediaBindingBo bo) {
        return toAjax(productBaseInfoService.saveProductMediaBinding(bo));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料绑定", businessType = BusinessType.UPDATE)
    @PutMapping("/media-bindings")
    @Operation(summary = "修改产品资料绑定")
    public R<Void> editProductMediaBinding(@Validated @RequestBody ProductMediaBindingBo bo) {
        return toAjax(productBaseInfoService.saveProductMediaBinding(bo));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "修改产品资料绑定状态", businessType = BusinessType.UPDATE)
    @PutMapping("/media-bindings/change-status/{id}/{status}")
    @Operation(summary = "修改产品资料绑定状态")
    public R<Void> changeProductMediaBindingStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseInfoService.updateProductMediaBindingStatus(id, status));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料绑定", businessType = BusinessType.DELETE)
    @DeleteMapping("/media-bindings/{ids}")
    @Operation(summary = "删除产品资料绑定")
    public R<Void> removeProductMediaBinding(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseInfoService.removeProductMediaBindingByIds(ids));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料批量绑定", businessType = BusinessType.INSERT)
    @PostMapping("/media-bindings/batch")
    @Operation(summary = "批量绑定产品资料")
    public R<Void> batchBindProductMedia(@Validated @RequestBody ProductMediaBindingBo bo) {
        return toAjax(productBaseInfoService.saveProductMediaBinding(bo));
    }

    @SaCheckPermission("product:asset:reference")
    @GetMapping("/media-bindings/{id}/references")
    @Operation(summary = "检查产品资料绑定引用")
    public R<ReferenceCheckResultVo> checkProductMediaBindingReferences(@PathVariable Long id) {
        return R.ok(productBaseInfoService.checkProductMediaBindingReferences(id));
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/models/list")
    @Operation(summary = "分页查询产品模型列表")
    public TableDataInfo<ProductModelVo> listProductModel(ProductModelBo bo, PageQuery pageQuery) {
        return productBaseInfoService.queryProductModelPage(bo, pageQuery);
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/models/options")
    @Operation(summary = "查询产品模型选项")
    public R<java.util.List<ProductModelVo>> optionsProductModel(ProductModelBo bo) {
        return R.ok(productBaseInfoService.queryProductModelList(bo));
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/models/{id}")
    @Operation(summary = "获取产品模型详情")
    public R<ProductModelVo> getProductModel(@Parameter(description = "产品模型ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseInfoService.getProductModelById(id));
    }

    @SaCheckPermission("product:model:add")
    @Log(title = "产品模型", businessType = BusinessType.INSERT)
    @PostMapping("/models")
    @Operation(summary = "新增产品模型")
    public R<Void> addProductModel(@Validated @RequestBody ProductModelBo bo) {
        return toAjax(productBaseInfoService.saveProductModel(bo));
    }

    @SaCheckPermission("product:model:edit")
    @Log(title = "产品模型", businessType = BusinessType.UPDATE)
    @PutMapping("/models")
    @Operation(summary = "修改产品模型")
    public R<Void> editProductModel(@Validated @RequestBody ProductModelBo bo) {
        return toAjax(productBaseInfoService.saveProductModel(bo));
    }

    @SaCheckPermission("product:model:edit")
    @Log(title = "修改产品模型状态", businessType = BusinessType.UPDATE)
    @PutMapping("/models/change-status/{id}/{status}")
    @Operation(summary = "修改产品模型状态")
    public R<Void> changeProductModelStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseInfoService.updateProductModelStatus(id, status));
    }

    @SaCheckPermission("product:model:remove")
    @Log(title = "产品模型", businessType = BusinessType.DELETE)
    @DeleteMapping("/models/{ids}")
    @Operation(summary = "删除产品模型")
    public R<Void> removeProductModel(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseInfoService.removeProductModelByIds(ids));
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/models/{id}/references")
    @Operation(summary = "检查产品模型引用")
    public R<ReferenceCheckResultVo> checkProductModelReferences(@PathVariable Long id) {
        return R.ok(productBaseInfoService.checkProductModelReferences(id));
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/models/{modelId}/variants")
    @Operation(summary = "查询产品模型销售变体")
    public R<java.util.List<SalesVariantVo>> listModelSalesVariant(@PathVariable Long modelId) {
        SalesVariantBo bo = new SalesVariantBo();
        bo.setModelId(modelId);
        return R.ok(productBaseInfoService.querySalesVariantList(bo));
    }

    @SaCheckPermission("product:model:edit")
    @Log(title = "产品模型销售变体", businessType = BusinessType.INSERT)
    @PostMapping("/models/{modelId}/variants")
    @Operation(summary = "新增产品模型销售变体")
    public R<Void> addModelSalesVariant(@PathVariable Long modelId, @Validated @RequestBody SalesVariantBo bo) {
        bo.setModelId(modelId);
        return toAjax(productBaseInfoService.saveSalesVariant(bo));
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/sales-variants/list")
    @Operation(summary = "分页查询产品销售变体列表")
    public TableDataInfo<SalesVariantVo> listSalesVariant(SalesVariantBo bo, PageQuery pageQuery) {
        return productBaseInfoService.querySalesVariantPage(bo, pageQuery);
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/sales-variants/options")
    @Operation(summary = "查询产品销售变体选项")
    public R<java.util.List<SalesVariantVo>> optionsSalesVariant(SalesVariantBo bo) {
        return R.ok(productBaseInfoService.querySalesVariantList(bo));
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/sales-variants/{id}")
    @Operation(summary = "获取产品销售变体详情")
    public R<SalesVariantVo> getSalesVariant(@Parameter(description = "产品销售变体ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseInfoService.getSalesVariantById(id));
    }

    @SaCheckPermission("product:model:add")
    @Log(title = "产品销售变体", businessType = BusinessType.INSERT)
    @PostMapping("/sales-variants")
    @Operation(summary = "新增产品销售变体")
    public R<Void> addSalesVariant(@Validated @RequestBody SalesVariantBo bo) {
        return toAjax(productBaseInfoService.saveSalesVariant(bo));
    }

    @SaCheckPermission("product:model:edit")
    @Log(title = "产品销售变体", businessType = BusinessType.UPDATE)
    @PutMapping("/sales-variants")
    @Operation(summary = "修改产品销售变体")
    public R<Void> editSalesVariant(@Validated @RequestBody SalesVariantBo bo) {
        return toAjax(productBaseInfoService.saveSalesVariant(bo));
    }

    @SaCheckPermission("product:model:edit")
    @Log(title = "修改产品销售变体状态", businessType = BusinessType.UPDATE)
    @PutMapping("/sales-variants/change-status/{id}/{status}")
    @Operation(summary = "修改产品销售变体状态")
    public R<Void> changeSalesVariantStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseInfoService.updateSalesVariantStatus(id, status));
    }

    @SaCheckPermission("product:model:remove")
    @Log(title = "产品销售变体", businessType = BusinessType.DELETE)
    @DeleteMapping("/sales-variants/{ids}")
    @Operation(summary = "删除产品销售变体")
    public R<Void> removeSalesVariant(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseInfoService.removeSalesVariantByIds(ids));
    }

    @SaCheckPermission("product:model:list")
    @GetMapping("/sales-variants/{id}/references")
    @Operation(summary = "检查产品销售变体引用")
    public R<ReferenceCheckResultVo> checkSalesVariantReferences(@PathVariable Long id) {
        return R.ok(productBaseInfoService.checkSalesVariantReferences(id));
    }

}
