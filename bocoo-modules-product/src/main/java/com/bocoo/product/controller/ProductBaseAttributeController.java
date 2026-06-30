package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductBaseAttributeBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductBaseAttributeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductBaseAttributeService;
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
 * 物料属性定义接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability/base-attributes")
@Tag(name = "物料属性定义", description = "物料属性定义增删改查、状态和引用检查接口")
public class ProductBaseAttributeController extends BaseController {

    private final ProductBaseAttributeService productBaseAttributeService;

    @SaCheckPermission("product:base-attribute:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询物料属性列表")
    public TableDataInfo<ProductBaseAttributeVo> list(ProductBaseAttributeBo bo, PageQuery pageQuery) {
        return productBaseAttributeService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:base-attribute:list")
    @GetMapping("/options")
    @Operation(summary = "查询物料属性选项")
    public R<List<ProductBaseAttributeVo>> options(ProductBaseAttributeBo bo) {
        return R.ok(productBaseAttributeService.queryList(bo));
    }

    @SaCheckPermission("product:base-attribute:list")
    @GetMapping("/{id}")
    @Operation(summary = "获取物料属性详情")
    public R<ProductBaseAttributeVo> getById(@Parameter(description = "物料属性ID", required = true) @PathVariable Long id) {
        return R.ok(productBaseAttributeService.queryById(id));
    }

    @SaCheckPermission("product:base-attribute:edit")
    @GetMapping("/{id}/edit-check")
    @Operation(summary = "检查物料属性是否可修改")
    public R<BaseEditCheckResultVo> checkEdit(@PathVariable Long id) {
        return R.ok(productBaseAttributeService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:base-attribute:add")
    @Log(title = "物料属性", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增物料属性")
    public R<Void> add(@Validated @RequestBody ProductBaseAttributeBo bo) {
        return toAjax(productBaseAttributeService.insertByBo(bo));
    }

    @SaCheckPermission("product:base-attribute:edit")
    @Log(title = "物料属性", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改物料属性")
    public R<Void> edit(@Validated @RequestBody ProductBaseAttributeBo bo) {
        return toAjax(productBaseAttributeService.updateByBo(bo));
    }

    @SaCheckPermission("product:base-attribute:edit")
    @Log(title = "修改物料属性状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改物料属性状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productBaseAttributeService.updateStatus(id, status));
    }

    @SaCheckPermission("product:base-attribute:remove")
    @Log(title = "物料属性", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除物料属性")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productBaseAttributeService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:base-attribute:reference")
    @GetMapping("/{id}/references")
    @Operation(summary = "检查物料属性引用")
    public R<ReferenceCheckResultVo> checkReferences(@PathVariable Long id) {
        return R.ok(productBaseAttributeService.checkReferences(id));
    }
}
