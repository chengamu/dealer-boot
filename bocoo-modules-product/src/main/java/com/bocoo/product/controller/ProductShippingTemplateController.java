package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductShippingTemplateBo;
import com.bocoo.product.domain.vo.ProductShippingTemplateVo;
import com.bocoo.product.service.ProductShippingTemplateService;
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
@RequestMapping("/product-pricing/shipping-templates")
@Tag(name = "邮费模板", description = "邮费模板维护接口")
public class ProductShippingTemplateController extends BaseController {

    private final ProductShippingTemplateService shippingTemplateService;

    @SaCheckPermission("product:shipping-template:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询邮费模板")
    public TableDataInfo<ProductShippingTemplateVo> list(ProductShippingTemplateBo bo, PageQuery pageQuery) {
        return shippingTemplateService.queryPageList(bo, pageQuery);
    }

    @Log(title = "邮费模板", businessType = BusinessType.EXPORT)
    @SaCheckPermission("product:shipping-template:export")
    @PostMapping("/export")
    @Operation(summary = "导出邮费模板")
    public void export(ProductShippingTemplateBo bo, HttpServletResponse response) {
        ExcelUtil.exportExcel(shippingTemplateService.queryList(bo), "邮费模板", ProductShippingTemplateVo.class, response);
    }

    @SaCheckPermission("product:shipping-template:list")
    @GetMapping("/options")
    @Operation(summary = "查询邮费模板选项")
    public R<List<ProductShippingTemplateVo>> options(ProductShippingTemplateBo bo) {
        return R.ok(shippingTemplateService.queryList(bo));
    }

    @SaCheckPermission("product:shipping-template:list")
    @GetMapping("/default")
    @Operation(summary = "查询默认邮费模板")
    public R<ProductShippingTemplateVo> defaultTemplate() {
        return R.ok(shippingTemplateService.queryDefaultEnabled());
    }

    @SaCheckPermission("product:shipping-template:list")
    @GetMapping("/{id}")
    @Operation(summary = "查询邮费模板详情")
    public R<ProductShippingTemplateVo> getById(@PathVariable Long id) {
        return R.ok(shippingTemplateService.queryById(id));
    }

    @SaCheckPermission("product:shipping-template:add")
    @Log(title = "邮费模板", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增邮费模板")
    public R<Void> add(@Validated @RequestBody ProductShippingTemplateBo bo) {
        return toAjax(shippingTemplateService.insertByBo(bo));
    }

    @SaCheckPermission("product:shipping-template:edit")
    @Log(title = "邮费模板", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改邮费模板")
    public R<Void> edit(@Validated @RequestBody ProductShippingTemplateBo bo) {
        return toAjax(shippingTemplateService.updateByBo(bo));
    }

    @SaCheckPermission("product:shipping-template:edit")
    @Log(title = "修改邮费模板状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改邮费模板状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(shippingTemplateService.updateStatus(id, status));
    }

    @SaCheckPermission("product:shipping-template:remove")
    @Log(title = "邮费模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除邮费模板")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(shippingTemplateService.deleteWithValidByIds(ids));
    }
}
