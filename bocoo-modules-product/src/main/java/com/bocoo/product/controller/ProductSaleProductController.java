package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductSaleProductBo;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.service.ProductSaleProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
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

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-pricing/sale-products")
@Tag(name = "可售产品", description = "可售产品管理接口")
public class ProductSaleProductController extends BaseController {

    private final ProductSaleProductService saleProductService;

    @SaCheckPermission("product:sale-product:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询可售产品")
    public TableDataInfo<ProductSaleProductVo> list(ProductSaleProductBo bo, PageQuery pageQuery) {
        return saleProductService.queryPageList(bo, pageQuery);
    }

    @Log(title = "可售产品", businessType = BusinessType.EXPORT)
    @SaCheckPermission("product:sale-product:export")
    @PostMapping("/export")
    @Operation(summary = "导出可售产品")
    public void export(ProductSaleProductBo bo, HttpServletResponse response) {
        ExcelUtil.exportExcel(saleProductService.queryList(bo), "可售产品", ProductSaleProductVo.class, response);
    }

    @SaCheckPermission("product:sale-product:list")
    @GetMapping("/options")
    @Operation(summary = "查询可售产品选项")
    public R<List<ProductSaleProductVo>> options(ProductSaleProductBo bo) {
        return R.ok(saleProductService.queryList(bo));
    }

    @SaCheckPermission("product:sale-product:query")
    @GetMapping("/{id}")
    @Operation(summary = "获取可售产品详情")
    public R<ProductSaleProductVo> get(@PathVariable Long id) {
        return R.ok(saleProductService.queryById(id));
    }

    @SaCheckPermission("product:sale-product:add")
    @Log(title = "可售产品", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增可售产品")
    public R<Void> add(@Validated @RequestBody ProductSaleProductBo bo) {
        return toAjax(saleProductService.insertByBo(bo));
    }

    @SaCheckPermission("product:sale-product:edit")
    @Log(title = "可售产品", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改可售产品")
    public R<Void> edit(@Validated @RequestBody ProductSaleProductBo bo) {
        return toAjax(saleProductService.updateByBo(bo));
    }

    @SaCheckPermission("product:sale-product:edit")
    @Log(title = "修改可售产品状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改可售产品状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(saleProductService.updateStatus(id, status));
    }

    @SaCheckPermission("product:sale-product:remove")
    @Log(title = "可售产品", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除可售产品")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(saleProductService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:sale-product:reference")
    @GetMapping("/{id}/references")
    @Operation(summary = "检查可售产品引用")
    public R<ReferenceCheckResultVo> references(@PathVariable Long id) {
        return R.ok(saleProductService.checkReferences(id));
    }
}
