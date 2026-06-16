package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductComponentBo;
import com.bocoo.product.domain.bo.ProductComponentItemBo;
import com.bocoo.product.domain.vo.ProductComponentItemVo;
import com.bocoo.product.domain.vo.ProductComponentVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductComponentItemService;
import com.bocoo.product.service.ProductComponentService;
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
 * 产品组件包和组件明细接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品组件包", description = "组件包和组件明细接口")
public class ProductComponentController extends BaseController {

    private final ProductComponentService productComponentService;
    private final ProductComponentItemService productComponentItemService;

    @SaCheckPermission("product:base:list")
    @GetMapping("/components/list")
    @Operation(summary = "分页查询产品组件列表")
    public TableDataInfo<ProductComponentVo> listProductComponent(ProductComponentBo bo, PageQuery pageQuery) {
        return productComponentService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/components/options")
    @Operation(summary = "查询产品组件选项")
    public R<java.util.List<ProductComponentVo>> optionsProductComponent(ProductComponentBo bo) {
        return R.ok(productComponentService.queryList(bo));
    }

    @SaCheckPermission("product:base:list")
    @GetMapping("/components/{id}")
    @Operation(summary = "获取产品组件详情")
    public R<ProductComponentVo> getProductComponent(@Parameter(description = "产品组件ID", required = true) @PathVariable Long id) {
        return R.ok(productComponentService.queryById(id));
    }

    @SaCheckPermission("product:base:add")
    @Log(title = "产品组件", businessType = BusinessType.INSERT)
    @PostMapping("/components")
    @Operation(summary = "新增产品组件")
    public R<Void> addProductComponent(@Validated @RequestBody ProductComponentBo bo) {
        return toAjax(productComponentService.insertByBo(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "产品组件", businessType = BusinessType.UPDATE)
    @PutMapping("/components")
    @Operation(summary = "修改产品组件")
    public R<Void> editProductComponent(@Validated @RequestBody ProductComponentBo bo) {
        return toAjax(productComponentService.updateByBo(bo));
    }

    @SaCheckPermission("product:base:edit")
    @Log(title = "修改产品组件状态", businessType = BusinessType.UPDATE)
    @PutMapping("/components/change-status/{id}/{status}")
    @Operation(summary = "修改产品组件状态")
    public R<Void> changeProductComponentStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productComponentService.updateStatus(id, status));
    }

    @SaCheckPermission("product:base:remove")
    @Log(title = "产品组件", businessType = BusinessType.DELETE)
    @DeleteMapping("/components/{ids}")
    @Operation(summary = "删除产品组件")
    public R<Void> removeProductComponent(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productComponentService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:base:reference")
    @GetMapping("/components/{id}/references")
    @Operation(summary = "检查产品组件引用")
    public R<ReferenceCheckResultVo> checkProductComponentReferences(@PathVariable Long id) {
        return R.ok(productComponentService.checkReferences(id));
    }

    @SaCheckPermission("product:component-item:list")
    @GetMapping("/component-items/list")
    @Operation(summary = "分页查询产品组件明细列表")
    public TableDataInfo<ProductComponentItemVo> listProductComponentItem(ProductComponentItemBo bo, PageQuery pageQuery) {
        return productComponentItemService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:component-item:list")
    @GetMapping("/component-items/options")
    @Operation(summary = "查询产品组件明细选项")
    public R<java.util.List<ProductComponentItemVo>> optionsProductComponentItem(ProductComponentItemBo bo) {
        return R.ok(productComponentItemService.queryList(bo));
    }

    @SaCheckPermission("product:component-item:list")
    @GetMapping("/component-items/{id}")
    @Operation(summary = "获取产品组件明细详情")
    public R<ProductComponentItemVo> getProductComponentItem(@Parameter(description = "产品组件明细ID", required = true) @PathVariable Long id) {
        return R.ok(productComponentItemService.queryById(id));
    }

    @SaCheckPermission("product:component-item:add")
    @Log(title = "产品组件明细", businessType = BusinessType.INSERT)
    @PostMapping("/component-items")
    @Operation(summary = "新增产品组件明细")
    public R<Void> addProductComponentItem(@Validated @RequestBody ProductComponentItemBo bo) {
        return toAjax(productComponentItemService.insertByBo(bo));
    }

    @SaCheckPermission("product:component-item:edit")
    @Log(title = "产品组件明细", businessType = BusinessType.UPDATE)
    @PutMapping("/component-items")
    @Operation(summary = "修改产品组件明细")
    public R<Void> editProductComponentItem(@Validated @RequestBody ProductComponentItemBo bo) {
        return toAjax(productComponentItemService.updateByBo(bo));
    }

    @SaCheckPermission("product:component-item:edit")
    @Log(title = "修改产品组件明细状态", businessType = BusinessType.UPDATE)
    @PutMapping("/component-items/change-status/{id}/{status}")
    @Operation(summary = "修改产品组件明细状态")
    public R<Void> changeProductComponentItemStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productComponentItemService.updateStatus(id, status));
    }

    @SaCheckPermission("product:component-item:remove")
    @Log(title = "产品组件明细", businessType = BusinessType.DELETE)
    @DeleteMapping("/component-items/{ids}")
    @Operation(summary = "删除产品组件明细")
    public R<Void> removeProductComponentItem(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productComponentItemService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:component-item:reference")
    @GetMapping("/component-items/{id}/references")
    @Operation(summary = "检查产品组件明细引用")
    public R<ReferenceCheckResultVo> checkProductComponentItemReferences(@PathVariable Long id) {
        return R.ok(productComponentItemService.checkReferences(id));
    }
}
