package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductMaterialAttributeBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialAttributeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductMaterialAttributeService;
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
 * 物料属性值接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability/material-attributes")
@Tag(name = "物料属性值", description = "物料属性值增删改查、状态和引用检查接口")
public class ProductMaterialAttributeController extends BaseController {

    private final ProductMaterialAttributeService productMaterialAttributeService;

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询物料属性值列表")
    public TableDataInfo<ProductMaterialAttributeVo> list(ProductMaterialAttributeBo bo, PageQuery pageQuery) {
        return productMaterialAttributeService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/options")
    @Operation(summary = "查询物料属性值选项")
    public R<List<ProductMaterialAttributeVo>> options(ProductMaterialAttributeBo bo) {
        return R.ok(productMaterialAttributeService.queryList(bo));
    }

    @SaCheckPermission("product:material-attribute:list")
    @GetMapping("/{id}")
    @Operation(summary = "获取物料属性值详情")
    public R<ProductMaterialAttributeVo> getById(@Parameter(description = "物料属性值ID", required = true) @PathVariable Long id) {
        return R.ok(productMaterialAttributeService.queryById(id));
    }

    @SaCheckPermission("product:material-attribute:edit")
    @GetMapping("/{id}/edit-check")
    @Operation(summary = "检查物料属性值是否可修改")
    public R<BaseEditCheckResultVo> checkEdit(@PathVariable Long id) {
        return R.ok(productMaterialAttributeService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:material-attribute:add")
    @Log(title = "物料属性值", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增物料属性值")
    public R<Void> add(@Validated @RequestBody ProductMaterialAttributeBo bo) {
        return toAjax(productMaterialAttributeService.insertByBo(bo));
    }

    @SaCheckPermission("product:material-attribute:edit")
    @Log(title = "物料属性值", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改物料属性值")
    public R<Void> edit(@Validated @RequestBody ProductMaterialAttributeBo bo) {
        return toAjax(productMaterialAttributeService.updateByBo(bo));
    }

    @SaCheckPermission("product:material-attribute:edit")
    @Log(title = "修改物料属性值状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改物料属性值状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMaterialAttributeService.updateStatus(id, status));
    }

    @SaCheckPermission("product:material-attribute:remove")
    @Log(title = "物料属性值", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除物料属性值")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMaterialAttributeService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:material-attribute:reference")
    @GetMapping("/{id}/references")
    @Operation(summary = "检查物料属性值引用")
    public R<ReferenceCheckResultVo> checkReferences(@PathVariable Long id) {
        return R.ok(productMaterialAttributeService.checkReferences(id));
    }
}
