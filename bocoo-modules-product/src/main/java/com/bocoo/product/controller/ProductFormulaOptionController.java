package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductFormulaSetupBo;
import com.bocoo.product.domain.vo.ProductFormulaSetupVo;
import com.bocoo.product.service.ProductFormulaSetupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-formula/formulas/{id}/options")
@Tag(name = "产品配方选项", description = "配置树、选项值、选项物料和异常限制接口")
public class ProductFormulaOptionController extends BaseController {

    private final ProductFormulaSetupService setupService;

    @SaCheckPermission("product:formula:setup")
    @GetMapping
    @Operation(summary = "获取配方选项")
    public R<ProductFormulaSetupVo> get(@PathVariable Long id) {
        return R.ok(setupService.querySetup(id));
    }

    @SaCheckPermission("product:formula:setup")
    @Log(title = "保存配方选项", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "保存配方选项")
    public R<Void> save(@PathVariable Long id, @Validated @RequestBody ProductFormulaSetupBo bo) {
        return toAjax(setupService.saveSetup(id, bo));
    }

    @SaCheckPermission("product:formula:setup")
    @Log(title = "校验配方选项", businessType = BusinessType.UPDATE)
    @PutMapping("/validate")
    @Operation(summary = "校验配方选项")
    public R<Void> validateOptions(@PathVariable Long id) {
        return toAjax(setupService.validateOptions(id));
    }
}
