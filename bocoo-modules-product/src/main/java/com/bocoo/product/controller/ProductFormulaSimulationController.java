package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductFormulaSimulationBo;
import com.bocoo.product.domain.vo.ProductFormulaSimulationVo;
import com.bocoo.product.service.ProductFormulaSimulationService;
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
@RequestMapping("/product-formula/formulas/{id}/simulation")
@Tag(name = "产品配方模拟", description = "配方模拟下单和模拟校验接口")
public class ProductFormulaSimulationController extends BaseController {

    private final ProductFormulaSimulationService simulationService;

    @SaCheckPermission("product:formula:setup")
    @GetMapping
    @Operation(summary = "获取配方模拟状态")
    public R<ProductFormulaSimulationVo> get(@PathVariable Long id) {
        return R.ok(simulationService.query(id));
    }

    @SaCheckPermission("product:formula:setup")
    @PostMapping("/run")
    @Operation(summary = "运行配方模拟")
    public R<ProductFormulaSimulationVo> run(@PathVariable Long id, @RequestBody(required = false) ProductFormulaSimulationBo bo) {
        return R.ok(simulationService.run(id, bo));
    }

    @SaCheckPermission("product:formula:setup")
    @Log(title = "校验配方模拟", businessType = BusinessType.UPDATE)
    @PutMapping("/validate")
    @Operation(summary = "校验配方模拟")
    public R<Void> validate(@PathVariable Long id, @RequestBody(required = false) ProductFormulaSimulationBo bo) {
        return toAjax(simulationService.validate(id, bo));
    }
}
