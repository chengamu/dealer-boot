package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductDictItemBo;
import com.bocoo.product.domain.bo.ProductDictTypeBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductDictItemVo;
import com.bocoo.product.domain.vo.ProductDictOptionVo;
import com.bocoo.product.domain.vo.ProductDictTypeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductDictItemService;
import com.bocoo.product.service.ProductDictTypeService;
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
 * 产品业务字典接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品业务字典", description = "产品业务字典类型和字典项接口")
public class ProductDictController extends BaseController {

    private final ProductDictTypeService productDictTypeService;
    private final ProductDictItemService productDictItemService;

    @SaCheckPermission("product:dict:list")
    @GetMapping("/product-dict-types/list")
    @Operation(summary = "分页查询产品业务字典类型")
    public TableDataInfo<ProductDictTypeVo> listDictType(ProductDictTypeBo bo, PageQuery pageQuery) {
        return productDictTypeService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:dict:list")
    @GetMapping("/product-dict-types/options")
    @Operation(summary = "查询产品业务字典类型选项")
    public R<List<ProductDictTypeVo>> optionsDictType(ProductDictTypeBo bo) {
        return R.ok(productDictTypeService.queryList(bo));
    }

    @SaCheckPermission("product:dict:list")
    @GetMapping("/product-dict-types/{id}")
    @Operation(summary = "获取产品业务字典类型详情")
    public R<ProductDictTypeVo> getDictType(@Parameter(description = "字典类型ID", required = true) @PathVariable Long id) {
        return R.ok(productDictTypeService.queryById(id));
    }

    @SaCheckPermission("product:dict:edit")
    @GetMapping("/product-dict-types/{id}/edit-check")
    @Operation(summary = "检查产品业务字典类型是否可修改")
    public R<BaseEditCheckResultVo> checkDictTypeEdit(@PathVariable Long id) {
        return R.ok(productDictTypeService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:dict:add")
    @Log(title = "产品业务字典类型", businessType = BusinessType.INSERT)
    @PostMapping("/product-dict-types")
    @Operation(summary = "新增产品业务字典类型")
    public R<Void> addDictType(@Validated @RequestBody ProductDictTypeBo bo) {
        return toAjax(productDictTypeService.insertByBo(bo));
    }

    @SaCheckPermission("product:dict:edit")
    @Log(title = "产品业务字典类型", businessType = BusinessType.UPDATE)
    @PutMapping("/product-dict-types")
    @Operation(summary = "修改产品业务字典类型")
    public R<Void> editDictType(@Validated @RequestBody ProductDictTypeBo bo) {
        return toAjax(productDictTypeService.updateByBo(bo));
    }

    @SaCheckPermission("product:dict:edit")
    @Log(title = "修改产品业务字典类型状态", businessType = BusinessType.UPDATE)
    @PutMapping("/product-dict-types/change-status/{id}/{status}")
    @Operation(summary = "修改产品业务字典类型状态")
    public R<Void> changeDictTypeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productDictTypeService.updateStatus(id, status));
    }

    @SaCheckPermission("product:dict:remove")
    @Log(title = "产品业务字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/product-dict-types/{ids}")
    @Operation(summary = "删除产品业务字典类型")
    public R<Void> removeDictType(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productDictTypeService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:dict:reference")
    @GetMapping("/product-dict-types/{id}/references")
    @Operation(summary = "检查产品业务字典类型引用")
    public R<ReferenceCheckResultVo> checkDictTypeReferences(@PathVariable Long id) {
        return R.ok(productDictTypeService.checkReferences(id));
    }

    @SaCheckPermission("product:dict:list")
    @GetMapping("/product-dict-items/list")
    @Operation(summary = "分页查询产品业务字典项")
    public TableDataInfo<ProductDictItemVo> listDictItem(ProductDictItemBo bo, PageQuery pageQuery) {
        return productDictItemService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:dict:list")
    @GetMapping("/product-dict-items/options")
    @Operation(summary = "查询产品业务字典项选项")
    public R<List<ProductDictItemVo>> optionsDictItem(ProductDictItemBo bo) {
        return R.ok(productDictItemService.queryList(bo));
    }

    @SaCheckPermission("product:dict:list")
    @GetMapping("/product-dict-items/type/{dictTypeCode}")
    @Operation(summary = "按字典类型查询产品业务字典选项")
    public R<List<ProductDictOptionVo>> optionsByType(@PathVariable String dictTypeCode) {
        return R.ok(productDictItemService.queryOptionsByType(dictTypeCode));
    }

    @SaCheckPermission("product:dict:list")
    @GetMapping("/product-dict-items/{id}")
    @Operation(summary = "获取产品业务字典项详情")
    public R<ProductDictItemVo> getDictItem(@Parameter(description = "字典项ID", required = true) @PathVariable Long id) {
        return R.ok(productDictItemService.queryById(id));
    }

    @SaCheckPermission("product:dict:edit")
    @GetMapping("/product-dict-items/{id}/edit-check")
    @Operation(summary = "检查产品业务字典项是否可修改")
    public R<BaseEditCheckResultVo> checkDictItemEdit(@PathVariable Long id) {
        return R.ok(productDictItemService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:dict:add")
    @Log(title = "产品业务字典项", businessType = BusinessType.INSERT)
    @PostMapping("/product-dict-items")
    @Operation(summary = "新增产品业务字典项")
    public R<Void> addDictItem(@Validated @RequestBody ProductDictItemBo bo) {
        return toAjax(productDictItemService.insertByBo(bo));
    }

    @SaCheckPermission("product:dict:edit")
    @Log(title = "产品业务字典项", businessType = BusinessType.UPDATE)
    @PutMapping("/product-dict-items")
    @Operation(summary = "修改产品业务字典项")
    public R<Void> editDictItem(@Validated @RequestBody ProductDictItemBo bo) {
        return toAjax(productDictItemService.updateByBo(bo));
    }

    @SaCheckPermission("product:dict:edit")
    @Log(title = "修改产品业务字典项状态", businessType = BusinessType.UPDATE)
    @PutMapping("/product-dict-items/change-status/{id}/{status}")
    @Operation(summary = "修改产品业务字典项状态")
    public R<Void> changeDictItemStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productDictItemService.updateStatus(id, status));
    }

    @SaCheckPermission("product:dict:remove")
    @Log(title = "产品业务字典项", businessType = BusinessType.DELETE)
    @DeleteMapping("/product-dict-items/{ids}")
    @Operation(summary = "删除产品业务字典项")
    public R<Void> removeDictItem(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productDictItemService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:dict:reference")
    @GetMapping("/product-dict-items/{id}/references")
    @Operation(summary = "检查产品业务字典项引用")
    public R<ReferenceCheckResultVo> checkDictItemReferences(@PathVariable Long id) {
        return R.ok(productDictItemService.checkReferences(id));
    }
}
