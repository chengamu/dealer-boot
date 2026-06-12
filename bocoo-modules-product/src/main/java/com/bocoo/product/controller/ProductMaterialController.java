package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductMaterialAttributeBo;
import com.bocoo.product.domain.bo.ProductMaterialBo;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 产品物料和物料属性接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品物料", description = "物料主档和通用物料属性接口")
public class ProductMaterialController extends BaseController {

    private final ProductMaterialService productMaterialService;

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/list")
    @Operation(summary = "分页查询产品物料列表")
    public TableDataInfo<ProductMaterialVo> listProductMaterial(ProductMaterialBo bo, PageQuery pageQuery) {
        return productMaterialService.queryProductMaterialPage(bo, pageQuery);
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/options")
    @Operation(summary = "查询产品物料选项")
    public R<java.util.List<ProductMaterialVo>> optionsProductMaterial(ProductMaterialBo bo) {
        return R.ok(productMaterialService.queryProductMaterialList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/{id}")
    @Operation(summary = "获取产品物料详情")
    public R<ProductMaterialVo> getProductMaterial(@Parameter(description = "产品物料ID", required = true) @PathVariable Long id) {
        return R.ok(productMaterialService.getProductMaterialById(id));
    }

    @SaCheckPermission("product:base:add")
    @Log(title = "产品物料", businessType = BusinessType.INSERT)
    @PostMapping("/materials")
    @Operation(summary = "新增产品物料")
    public R<Void> addProductMaterial(@Validated @RequestBody ProductMaterialBo bo) {
        return toAjax(productMaterialService.saveProductMaterial(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "产品物料", businessType = BusinessType.UPDATE)
    @PutMapping("/materials")
    @Operation(summary = "修改产品物料")
    public R<Void> editProductMaterial(@Validated @RequestBody ProductMaterialBo bo) {
        return toAjax(productMaterialService.saveProductMaterial(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "修改产品物料状态", businessType = BusinessType.UPDATE)
    @PutMapping("/materials/change-status/{id}/{status}")
    @Operation(summary = "修改产品物料状态")
    public R<Void> changeProductMaterialStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMaterialService.updateProductMaterialStatus(id, status));
    }

    @SaCheckPermission("product:base:remove")
    @Log(title = "产品物料", businessType = BusinessType.DELETE)
    @DeleteMapping("/materials/{ids}")
    @Operation(summary = "删除产品物料")
    public R<Void> removeProductMaterial(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMaterialService.removeProductMaterialByIds(ids));
    }

    @SaCheckPermission("product:base:reference")
    @GetMapping("/materials/{id}/references")
    @Operation(summary = "检查产品物料引用")
    public R<ReferenceCheckResultVo> checkProductMaterialReferences(@PathVariable Long id) {
        return R.ok(productMaterialService.checkProductMaterialReferences(id));
    }

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/material-attributes/list")
    @Operation(summary = "分页查询物料属性值列表")
    public TableDataInfo<ProductMaterialAttributeVo> listProductMaterialAttribute(ProductMaterialAttributeBo bo, PageQuery pageQuery) {
        return productMaterialService.queryProductMaterialAttributePage(bo, pageQuery);
    }

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/material-attributes/options")
    @Operation(summary = "查询物料属性值选项")
    public R<java.util.List<ProductMaterialAttributeVo>> optionsProductMaterialAttribute(ProductMaterialAttributeBo bo) {
        return R.ok(productMaterialService.queryProductMaterialAttributeList(bo));
    }

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/material-attributes/{id}")
    @Operation(summary = "获取物料属性值详情")
    public R<ProductMaterialAttributeVo> getProductMaterialAttribute(@Parameter(description = "物料属性值ID", required = true) @PathVariable Long id) {
        return R.ok(productMaterialService.getProductMaterialAttributeById(id));
    }

    @SaCheckPermission("product:material-attribute:add")
    @Log(title = "物料属性值", businessType = BusinessType.INSERT)
    @PostMapping("/material-attributes")
    @Operation(summary = "新增物料属性值")
    public R<Void> addProductMaterialAttribute(@Validated @RequestBody ProductMaterialAttributeBo bo) {
        return toAjax(productMaterialService.saveProductMaterialAttribute(bo));
    }

    @SaCheckPermission("product:material-attribute:edit")
    @Log(title = "物料属性值", businessType = BusinessType.UPDATE)
    @PutMapping("/material-attributes")
    @Operation(summary = "修改物料属性值")
    public R<Void> editProductMaterialAttribute(@Validated @RequestBody ProductMaterialAttributeBo bo) {
        return toAjax(productMaterialService.saveProductMaterialAttribute(bo));
    }

    @SaCheckPermission("product:material-attribute:edit")
    @Log(title = "修改物料属性值状态", businessType = BusinessType.UPDATE)
    @PutMapping("/material-attributes/change-status/{id}/{status}")
    @Operation(summary = "修改物料属性值状态")
    public R<Void> changeProductMaterialAttributeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMaterialService.updateProductMaterialAttributeStatus(id, status));
    }

    @SaCheckPermission("product:material-attribute:remove")
    @Log(title = "物料属性值", businessType = BusinessType.DELETE)
    @DeleteMapping("/material-attributes/{ids}")
    @Operation(summary = "删除物料属性值")
    public R<Void> removeProductMaterialAttribute(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMaterialService.removeProductMaterialAttributeByIds(ids));
    }

    @SaCheckPermission("product:material-attribute:reference")
    @GetMapping("/material-attributes/{id}/references")
    @Operation(summary = "检查物料属性值引用")
    public R<ReferenceCheckResultVo> checkProductMaterialAttributeReferences(@PathVariable Long id) {
        return R.ok(productMaterialService.checkProductMaterialAttributeReferences(id));
    }
}
