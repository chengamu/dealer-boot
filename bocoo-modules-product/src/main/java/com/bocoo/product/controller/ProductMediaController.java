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
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMediaAssetVo;
import com.bocoo.product.domain.vo.ProductMediaBindingVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductMediaAssetService;
import com.bocoo.product.service.ProductMediaBindingService;
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
 * 产品附件资料接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品附件资料", description = "资料资产和资料绑定接口")
public class ProductMediaController extends BaseController {

    private final ProductMediaAssetService productMediaAssetService;
    private final ProductMediaBindingService productMediaBindingService;

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/list")
    @Operation(summary = "分页查询产品资料资产列表")
    public TableDataInfo<ProductMediaAssetVo> listProductMediaAsset(ProductMediaAssetBo bo, PageQuery pageQuery) {
        return productMediaAssetService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/options")
    @Operation(summary = "查询产品资料资产选项")
    public R<java.util.List<ProductMediaAssetVo>> optionsProductMediaAsset(ProductMediaAssetBo bo) {
        return R.ok(productMediaAssetService.queryList(bo));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-assets/{id}")
    @Operation(summary = "获取产品资料资产详情")
    public R<ProductMediaAssetVo> getProductMediaAsset(@Parameter(description = "产品资料资产ID", required = true) @PathVariable Long id) {
        return R.ok(productMediaAssetService.queryById(id));
    }

    @SaCheckPermission("product:asset:edit")
    @GetMapping("/media-assets/{id}/edit-check")
    @Operation(summary = "检查产品资料资产是否可修改")
    public R<BaseEditCheckResultVo> checkProductMediaAssetEdit(@PathVariable Long id) {
        return R.ok(productMediaAssetService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:asset:upload")
    @Log(title = "产品资料资产", businessType = BusinessType.INSERT)
    @PostMapping("/media-assets")
    @Operation(summary = "新增产品资料资产")
    public R<Void> addProductMediaAsset(@Validated @RequestBody ProductMediaAssetBo bo) {
        return toAjax(productMediaAssetService.insertByBo(bo));
    }

    @SaCheckPermission("product:asset:edit")
    @Log(title = "产品资料资产", businessType = BusinessType.UPDATE)
    @PutMapping("/media-assets")
    @Operation(summary = "修改产品资料资产")
    public R<Void> editProductMediaAsset(@Validated @RequestBody ProductMediaAssetBo bo) {
        return toAjax(productMediaAssetService.updateByBo(bo));
    }

    @SaCheckPermission("product:asset:edit")
    @Log(title = "修改产品资料资产状态", businessType = BusinessType.UPDATE)
    @PutMapping("/media-assets/change-status/{id}/{status}")
    @Operation(summary = "修改产品资料资产状态")
    public R<Void> changeProductMediaAssetStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMediaAssetService.updateStatus(id, status));
    }

    @SaCheckPermission("product:asset:remove")
    @Log(title = "产品资料资产", businessType = BusinessType.DELETE)
    @DeleteMapping("/media-assets/{ids}")
    @Operation(summary = "删除产品资料资产")
    public R<Void> removeProductMediaAsset(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMediaAssetService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:asset:reference")
    @GetMapping("/media-assets/{id}/references")
    @Operation(summary = "检查产品资料资产引用")
    public R<ReferenceCheckResultVo> checkProductMediaAssetReferences(@PathVariable Long id) {
        return R.ok(productMediaAssetService.checkReferences(id));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/list")
    @Operation(summary = "分页查询产品资料绑定列表")
    public TableDataInfo<ProductMediaBindingVo> listProductMediaBinding(ProductMediaBindingBo bo, PageQuery pageQuery) {
        return productMediaBindingService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/options")
    @Operation(summary = "查询产品资料绑定选项")
    public R<java.util.List<ProductMediaBindingVo>> optionsProductMediaBinding(ProductMediaBindingBo bo) {
        return R.ok(productMediaBindingService.queryList(bo));
    }

    @SaCheckPermission("product:asset:list")
    @GetMapping("/media-bindings/{id}")
    @Operation(summary = "获取产品资料绑定详情")
    public R<ProductMediaBindingVo> getProductMediaBinding(@Parameter(description = "产品资料绑定ID", required = true) @PathVariable Long id) {
        return R.ok(productMediaBindingService.queryById(id));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料绑定", businessType = BusinessType.INSERT)
    @PostMapping("/media-bindings")
    @Operation(summary = "新增产品资料绑定")
    public R<Void> addProductMediaBinding(@Validated @RequestBody ProductMediaBindingBo bo) {
        return toAjax(productMediaBindingService.insertByBo(bo));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "产品资料绑定", businessType = BusinessType.UPDATE)
    @PutMapping("/media-bindings")
    @Operation(summary = "修改产品资料绑定")
    public R<Void> editProductMediaBinding(@Validated @RequestBody ProductMediaBindingBo bo) {
        return toAjax(productMediaBindingService.updateByBo(bo));
    }

    @SaCheckPermission("product:asset:bind")
    @Log(title = "修改产品资料绑定状态", businessType = BusinessType.UPDATE)
    @PutMapping("/media-bindings/change-status/{id}/{status}")
    @Operation(summary = "修改产品资料绑定状态")
    public R<Void> changeProductMediaBindingStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMediaBindingService.updateStatus(id, status));
    }

    @SaCheckPermission("product:asset:unbind")
    @Log(title = "产品资料绑定", businessType = BusinessType.DELETE)
    @DeleteMapping("/media-bindings/{ids}")
    @Operation(summary = "删除产品资料绑定")
    public R<Void> removeProductMediaBinding(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMediaBindingService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:asset:reference")
    @GetMapping("/media-bindings/{id}/references")
    @Operation(summary = "检查产品资料绑定引用")
    public R<ReferenceCheckResultVo> checkProductMediaBindingReferences(@PathVariable Long id) {
        return R.ok(productMediaBindingService.checkReferences(id));
    }
}
