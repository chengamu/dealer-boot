package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductMaterialTypeBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductMaterialTypeVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductMaterialTypeService;
import io.swagger.v3.oas.annotations.Operation;
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
 * 物料类型接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability/material-types")
@Tag(name = "物料类型", description = "物料类型增删改查、状态和引用检查接口")
public class ProductMaterialTypeController extends BaseController {

    private final ProductMaterialTypeService productMaterialTypeService;

    @SaCheckPermission("product:material-type:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询物料类型列表")
    public TableDataInfo<ProductMaterialTypeVo> list(ProductMaterialTypeBo bo, PageQuery pageQuery) {
        return productMaterialTypeService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:material-type:list")
    @GetMapping("/options")
    @Operation(summary = "查询物料类型选项")
    public R<List<ProductMaterialTypeVo>> options(ProductMaterialTypeBo bo) {
        return R.ok(productMaterialTypeService.queryList(bo));
    }

    @SaCheckPermission("product:material-type:list")
    @GetMapping("/{id}")
    @Operation(summary = "获取物料类型详情")
    public R<ProductMaterialTypeVo> getById(@PathVariable Long id) {
        return R.ok(productMaterialTypeService.queryById(id));
    }

    @SaCheckPermission("product:material-type:edit")
    @GetMapping("/{id}/edit-check")
    @Operation(summary = "检查物料类型是否可修改")
    public R<BaseEditCheckResultVo> checkEdit(@PathVariable Long id) {
        return R.ok(productMaterialTypeService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:material-type:add")
    @Log(title = "物料类型", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增物料类型")
    public R<Void> add(@Validated @RequestBody ProductMaterialTypeBo bo) {
        return toAjax(productMaterialTypeService.insertByBo(bo));
    }

    @SaCheckPermission("product:material-type:edit")
    @Log(title = "物料类型", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改物料类型")
    public R<Void> edit(@Validated @RequestBody ProductMaterialTypeBo bo) {
        return toAjax(productMaterialTypeService.updateByBo(bo));
    }

    @SaCheckPermission("product:material-type:edit")
    @Log(title = "修改物料类型状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改物料类型状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productMaterialTypeService.updateStatus(id, status));
    }

    @SaCheckPermission("product:material-type:remove")
    @Log(title = "物料类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除物料类型")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productMaterialTypeService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:material-type:reference")
    @GetMapping("/{id}/references")
    @Operation(summary = "检查物料类型引用")
    public R<ReferenceCheckResultVo> checkReferences(@PathVariable Long id) {
        return R.ok(productMaterialTypeService.checkReferences(id));
    }
}
