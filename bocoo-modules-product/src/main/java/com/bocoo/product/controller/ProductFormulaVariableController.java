package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.service.ProductFormulaVariableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-formula/formulas/{id}/variables")
@Tag(name = "产品配方内部变量", description = "配方内部变量池接口")
public class ProductFormulaVariableController extends BaseController {

    private final ProductFormulaVariableService variableService;

    @SaCheckPermission("product:formula:setup")
    @GetMapping
    @Operation(summary = "获取配方内部变量")
    public R<ProductFormulaSetupVo> get(@PathVariable Long id) {
        return R.ok(variableService.queryVariables(id));
    }

    @SaCheckPermission("product:formula:setup")
    @Log(title = "保存配方内部变量", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "保存配方内部变量")
    public R<Void> save(@PathVariable Long id, @Validated @RequestBody ProductFormulaSetupBo bo) {
        return toAjax(variableService.saveVariables(id, bo));
    }

    @SaCheckPermission("product:formula:setup")
    @Log(title = "复制配方内部变量", businessType = BusinessType.UPDATE)
    @PostMapping("/copy-from-code/{sourceFormulaCode}")
    @Operation(summary = "从已有配方复制内部变量")
    public R<Void> copyFrom(@PathVariable Long id, @PathVariable String sourceFormulaCode) {
        return toAjax(variableService.copyFrom(id, sourceFormulaCode));
    }

    @SaCheckPermission("product:formula:setup")
    @Log(title = "校验配方内部变量", businessType = BusinessType.UPDATE)
    @PutMapping("/validate")
    @Operation(summary = "校验配方内部变量")
    public R<Void> validateVariables(@PathVariable Long id) {
        return toAjax(variableService.validateVariables(id));
    }
}
