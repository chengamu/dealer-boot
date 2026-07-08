package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductPriceFabricRuleBo;
import com.bocoo.product.domain.bo.ProductPriceFeeRuleBo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import com.bocoo.product.service.ProductPriceSettingService;
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

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-pricing/price-settings")
@Tag(name = "价格设置", description = "产品价格设置接口")
public class ProductPriceSettingController extends BaseController {

    private final ProductPriceSettingService priceSettingService;

    @SaCheckPermission("product:pricing:query")
    @GetMapping("/{saleProductId}")
    @Operation(summary = "获取价格设置工作台")
    public R<ProductPriceSetupVo> setup(@PathVariable Long saleProductId) {
        return R.ok(priceSettingService.querySetup(saleProductId));
    }

    @SaCheckPermission("product:pricing:edit")
    @Log(title = "生成面料价格", businessType = BusinessType.UPDATE)
    @PutMapping("/{saleProductId}/generate-fabric-prices/{overwrite}")
    @Operation(summary = "按配方版本生成面料价格")
    public R<Void> generateFabricPrices(@PathVariable Long saleProductId, @PathVariable Boolean overwrite) {
        return toAjax(priceSettingService.generateFabricPrices(saleProductId, overwrite));
    }

    @SaCheckPermission("product:pricing:edit")
    @Log(title = "保存面料价格规则", businessType = BusinessType.UPDATE)
    @PutMapping("/{saleProductId}/fabrics/{priceFabricId}/rules")
    @Operation(summary = "保存面料价格规则")
    public R<Void> saveFabricRules(@PathVariable Long saleProductId, @PathVariable Long priceFabricId,
                                   @RequestBody List<ProductPriceFabricRuleBo> rules) {
        return toAjax(priceSettingService.saveFabricRules(saleProductId, priceFabricId, rules));
    }

    @SaCheckPermission("product:pricing:edit")
    @Log(title = "保存附加费用规则", businessType = BusinessType.UPDATE)
    @PutMapping("/{saleProductId}/fee-rules")
    @Operation(summary = "保存附加费用规则")
    public R<Void> saveFeeRules(@PathVariable Long saleProductId, @RequestBody List<ProductPriceFeeRuleBo> rules) {
        return toAjax(priceSettingService.saveFeeRules(saleProductId, rules));
    }

    @SaCheckPermission("product:pricing:validate")
    @Log(title = "校验产品价格", businessType = BusinessType.UPDATE)
    @PutMapping("/{saleProductId}/validate")
    @Operation(summary = "校验产品价格")
    public R<List<ProductPriceValidationIssueVo>> validatePrice(@PathVariable Long saleProductId) {
        return R.ok(priceSettingService.validatePrice(saleProductId));
    }
}
