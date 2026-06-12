package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductMediaAssetBo;
import com.bocoo.product.domain.bo.ProductMediaBindingBo;
import com.bocoo.product.domain.vo.ProductMediaAssetVo;
import com.bocoo.product.domain.vo.ProductMediaBindingVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 产品附件资料接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品附件资料", description = "资料资产和资料绑定接口")
public class ProductMediaController extends BaseController {

    private final ProductMediaService productMediaService;

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/list")
    @Operation(summary = "分页查询产品资料资产列表")
    public TableDataInfo<ProductMediaAssetVo> listProductMediaAsset(ProductMediaAssetBo bo, PageQuery pageQuery) {
        return productMediaService.queryProductMediaAssetPage(bo, pageQuery);
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/options")
    @Operation(summary = "查询产品资料资产选项")
    public R<java.util.List<ProductMediaAssetVo>> optionsProductMediaAsset(ProductMediaAssetBo bo) {
        return R.ok(productMediaService.queryProductMediaAssetList(bo));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/{id}")
    @Operation(summary = "获取产品资料资产详情")
    public R<ProductMediaAssetVo> getProductMediaAsset(@Parameter(description = "产品资料资产ID", required = true) @PathVariable Long id) {
        return R.ok(productMediaService.getProductMediaAssetById(id));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "产品资料资产", businessType = BusinessType.INSERT)
    @PostMapping("/media-assets")
    @Operation(summary = "新增产品资料资产")
    public R<Void> addProductMediaAsset(@Validated @RequestBody ProductMediaAssetBo bo) {
        return toAjax(productMediaService.saveProductMediaAsset(bo));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "产品资料资产", businessType = BusinessType.UPDATE)
    @PutMapping("/media-assets")
    @Operation(summary = "修改产品资料资产")
    public R<Void> editProductMediaAsset(@Validated @RequestBody ProductMediaAssetBo bo) {
        return toAjax(productMediaService.saveProductMediaAsset(bo));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "修改产品资料资产状态", businessType = BusinessType.UPDATE)
    @PutMapping("/media-assets/change-status/{id}/{status}")
    @Operation(summary = "修改产品资料资产状态")
    public R<Void> changeProductMediaAssetStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMediaService.updateProductMediaAssetStatus(id, status));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "产品资料资产", businessType = BusinessType.DELETE)
    @DeleteMapping("/media-assets/{ids}")
    @Operation(summary = "删除产品资料资产")
    public R<Void> removeProductMediaAsset(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMediaService.removeProductMediaAssetByIds(ids));
    }

    @SaCheckPermission("product:asset:reference")
    @GetMapping("/media-assets/{id}/references")
    @Operation(summary = "检查产品资料资产引用")
    public R<ReferenceCheckResultVo> checkProductMediaAssetReferences(@PathVariable Long id) {
        return R.ok(productMediaService.checkProductMediaAssetReferences(id));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/list")
    @Operation(summary = "分页查询产品资料绑定列表")
    public TableDataInfo<ProductMediaBindingVo> listProductMediaBinding(ProductMediaBindingBo bo, PageQuery pageQuery) {
        return productMediaService.queryProductMediaBindingPage(bo, pageQuery);
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/options")
    @Operation(summary = "查询产品资料绑定选项")
    public R<java.util.List<ProductMediaBindingVo>> optionsProductMediaBinding(ProductMediaBindingBo bo) {
        return R.ok(productMediaService.queryProductMediaBindingList(bo));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/{id}")
    @Operation(summary = "获取产品资料绑定详情")
    public R<ProductMediaBindingVo> getProductMediaBinding(@Parameter(description = "产品资料绑定ID", required = true) @PathVariable Long id) {
        return R.ok(productMediaService.getProductMediaBindingById(id));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料绑定", businessType = BusinessType.INSERT)
    @PostMapping("/media-bindings")
    @Operation(summary = "新增产品资料绑定")
    public R<Void> addProductMediaBinding(@Validated @RequestBody ProductMediaBindingBo bo) {
        return toAjax(productMediaService.saveProductMediaBinding(bo));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料绑定", businessType = BusinessType.UPDATE)
    @PutMapping("/media-bindings")
    @Operation(summary = "修改产品资料绑定")
    public R<Void> editProductMediaBinding(@Validated @RequestBody ProductMediaBindingBo bo) {
        return toAjax(productMediaService.saveProductMediaBinding(bo));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "修改产品资料绑定状态", businessType = BusinessType.UPDATE)
    @PutMapping("/media-bindings/change-status/{id}/{status}")
    @Operation(summary = "修改产品资料绑定状态")
    public R<Void> changeProductMediaBindingStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMediaService.updateProductMediaBindingStatus(id, status));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料绑定", businessType = BusinessType.DELETE)
    @DeleteMapping("/media-bindings/{ids}")
    @Operation(summary = "删除产品资料绑定")
    public R<Void> removeProductMediaBinding(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMediaService.removeProductMediaBindingByIds(ids));
    }

    @SaCheckPermission("product:asset:reference")
    @GetMapping("/media-bindings/{id}/references")
    @Operation(summary = "检查产品资料绑定引用")
    public R<ReferenceCheckResultVo> checkProductMediaBindingReferences(@PathVariable Long id) {
        return R.ok(productMediaService.checkProductMediaBindingReferences(id));
    }
}
