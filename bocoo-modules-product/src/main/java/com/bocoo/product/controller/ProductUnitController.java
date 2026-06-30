package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductUnitBo;
import com.bocoo.product.domain.vo.BaseEditCheckResultVo;
import com.bocoo.product.domain.vo.ProductUnitVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductUnitService;
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
 * 产品单位接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability/units")
@Tag(name = "产品单位", description = "产品单位增删改查、状态和引用检查接口")
public class ProductUnitController extends BaseController {

    private final ProductUnitService productUnitService;

    @SaCheckPermission("product:unit:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询产品单位列表")
    public TableDataInfo<ProductUnitVo> list(ProductUnitBo bo, PageQuery pageQuery) {
        return productUnitService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:unit:list")
    @GetMapping("/options")
    @Operation(summary = "查询产品单位选项")
    public R<List<ProductUnitVo>> options(ProductUnitBo bo) {
        return R.ok(productUnitService.queryList(bo));
    }

    @SaCheckPermission("product:unit:list")
    @GetMapping("/{id}")
    @Operation(summary = "获取产品单位详情")
    public R<ProductUnitVo> getById(@Parameter(description = "产品单位ID", required = true) @PathVariable Long id) {
        return R.ok(productUnitService.queryById(id));
    }

    @SaCheckPermission("product:unit:edit")
    @GetMapping("/{id}/edit-check")
    @Operation(summary = "检查产品单位是否可修改")
    public R<BaseEditCheckResultVo> checkEdit(@PathVariable Long id) {
        return R.ok(productUnitService.checkEditAllowed(id));
    }

    @SaCheckPermission("product:unit:add")
    @Log(title = "产品单位", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增产品单位")
    public R<Void> add(@Validated @RequestBody ProductUnitBo bo) {
        return toAjax(productUnitService.insertByBo(bo));
    }

    @SaCheckPermission("product:unit:edit")
    @Log(title = "产品单位", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改产品单位")
    public R<Void> edit(@Validated @RequestBody ProductUnitBo bo) {
        return toAjax(productUnitService.updateByBo(bo));
    }

    @SaCheckPermission("product:unit:edit")
    @Log(title = "修改产品单位状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改产品单位状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productUnitService.updateStatus(id, status));
    }

    @SaCheckPermission("product:unit:remove")
    @Log(title = "产品单位", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除产品单位")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productUnitService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:unit:reference")
    @GetMapping("/{id}/references")
    @Operation(summary = "检查产品单位引用")
    public R<ReferenceCheckResultVo> checkReferences(@PathVariable Long id) {
        return R.ok(productUnitService.checkReferences(id));
    }
}
