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
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.domain.vo.ProductMaterialVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductMaterialAttributeService;
import com.bocoo.product.service.ProductMaterialService;
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
 * 产品物料和物料属性接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品物料", description = "物料主档和通用物料属性接口")
public class ProductMaterialController extends BaseController {

    private final ProductMaterialService productMaterialService;
    private final ProductMaterialAttributeService productMaterialAttributeService;

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/list")
    @Operation(summary = "分页查询产品物料列表")
    public TableDataInfo<ProductMaterialVo> listProductMaterial(ProductMaterialBo bo, PageQuery pageQuery) {
        return productMaterialService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/options")
    @Operation(summary = "查询产品物料选项")
    public R<java.util.List<ProductMaterialVo>> optionsProductMaterial(ProductMaterialBo bo) {
        return R.ok(productMaterialService.queryList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/materials/{id}")
    @Operation(summary = "获取产品物料详情")
    public R<ProductMaterialVo> getProductMaterial(@Parameter(description = "产品物料ID", required = true) @PathVariable Long id) {
        return R.ok(productMaterialService.queryById(id));
    }

    @SaCheckPermission("product:base:edit")
    @GetMapping("/materials/{id}/edit-check")
    @Operation(summary = "检查产品物料是否可修改")
    public R<BaseEditCheckResultVo> checkProductMaterialEdit(@PathVariable Long id) {
        return R.ok(productMaterialService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:base:add")
    @Log(title = "产品物料", businessType = BusinessType.INSERT)
    @PostMapping("/materials")
    @Operation(summary = "新增产品物料")
    public R<Void> addProductMaterial(@Validated @RequestBody ProductMaterialBo bo) {
        return toAjax(productMaterialService.insertByBo(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "产品物料", businessType = BusinessType.UPDATE)
    @PutMapping("/materials")
    @Operation(summary = "修改产品物料")
    public R<Void> editProductMaterial(@Validated @RequestBody ProductMaterialBo bo) {
        return toAjax(productMaterialService.updateByBo(bo));
    }

    @SaCheckPermission("product:base:superEdit")
    @Log(title = "产品物料超级修改", businessType = BusinessType.UPDATE)
    @PutMapping("/materials/super-update")
    @Operation(summary = "超级修改产品物料")
    public R<Void> superEditProductMaterial(@Validated @RequestBody ProductMaterialBo bo) {
        return toAjax(productMaterialService.superUpdateByBo(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "修改产品物料状态", businessType = BusinessType.UPDATE)
    @PutMapping("/materials/change-status/{id}/{status}")
    @Operation(summary = "修改产品物料状态")
    public R<Void> changeProductMaterialStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMaterialService.updateStatus(id, status));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "审核产品物料", businessType = BusinessType.UPDATE)
    @PutMapping("/materials/audit/{id}")
    @Operation(summary = "审核产品物料")
    public R<Void> auditProductMaterial(@PathVariable Long id) {
        return toAjax(productMaterialService.audit(id));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "取消审核产品物料", businessType = BusinessType.UPDATE)
    @PutMapping("/materials/unaudit/{id}")
    @Operation(summary = "取消审核产品物料")
    public R<Void> unauditProductMaterial(@PathVariable Long id) {
        return toAjax(productMaterialService.unaudit(id));
    }

    @SaCheckPermission("product:base:remove")
    @Log(title = "产品物料", businessType = BusinessType.DELETE)
    @DeleteMapping("/materials/{ids}")
    @Operation(summary = "删除产品物料")
    public R<Void> removeProductMaterial(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMaterialService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:base:reference")
    @GetMapping("/materials/{id}/references")
    @Operation(summary = "检查产品物料引用")
    public R<ReferenceCheckResultVo> checkProductMaterialReferences(@PathVariable Long id) {
        return R.ok(productMaterialService.checkReferences(id));
    }

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/material-attributes/list")
    @Operation(summary = "分页查询物料属性值列表")
    public TableDataInfo<ProductMaterialAttributeVo> listProductMaterialAttribute(ProductMaterialAttributeBo bo, PageQuery pageQuery) {
        return productMaterialAttributeService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/material-attributes/options")
    @Operation(summary = "查询物料属性值选项")
    public R<java.util.List<ProductMaterialAttributeVo>> optionsProductMaterialAttribute(ProductMaterialAttributeBo bo) {
        return R.ok(productMaterialAttributeService.queryList(bo));
    }

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/material-attributes/{id}")
    @Operation(summary = "获取物料属性值详情")
    public R<ProductMaterialAttributeVo> getProductMaterialAttribute(@Parameter(description = "物料属性值ID", required = true) @PathVariable Long id) {
        return R.ok(productMaterialAttributeService.queryById(id));
    }

    @SaCheckPermission("product:material-attribute:edit")
    @GetMapping("/material-attributes/{id}/edit-check")
    @Operation(summary = "检查物料属性值是否可修改")
    public R<BaseEditCheckResultVo> checkProductMaterialAttributeEdit(@PathVariable Long id) {
        return R.ok(productMaterialAttributeService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:material-attribute:add")
    @Log(title = "物料属性值", businessType = BusinessType.INSERT)
    @PostMapping("/material-attributes")
    @Operation(summary = "新增物料属性值")
    public R<Void> addProductMaterialAttribute(@Validated @RequestBody ProductMaterialAttributeBo bo) {
        return toAjax(productMaterialAttributeService.insertByBo(bo));
    }

    @SaCheckPermission("product:material-attribute:edit")
    @Log(title = "物料属性值", businessType = BusinessType.UPDATE)
    @PutMapping("/material-attributes")
    @Operation(summary = "修改物料属性值")
    public R<Void> editProductMaterialAttribute(@Validated @RequestBody ProductMaterialAttributeBo bo) {
        return toAjax(productMaterialAttributeService.updateByBo(bo));
    }

    @SaCheckPermission("product:material-attribute:edit")
    @Log(title = "修改物料属性值状态", businessType = BusinessType.UPDATE)
    @PutMapping("/material-attributes/change-status/{id}/{status}")
    @Operation(summary = "修改物料属性值状态")
    public R<Void> changeProductMaterialAttributeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMaterialAttributeService.updateStatus(id, status));
    }

    @SaCheckPermission("product:material-attribute:remove")
    @Log(title = "物料属性值", businessType = BusinessType.DELETE)
    @DeleteMapping("/material-attributes/{ids}")
    @Operation(summary = "删除物料属性值")
    public R<Void> removeProductMaterialAttribute(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMaterialAttributeService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:material-attribute:reference")
    @GetMapping("/material-attributes/{id}/references")
    @Operation(summary = "检查物料属性值引用")
    public R<ReferenceCheckResultVo> checkProductMaterialAttributeReferences(@PathVariable Long id) {
        return R.ok(productMaterialAttributeService.checkReferences(id));
    }
}
