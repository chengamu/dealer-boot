package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductPriceMaterialBatchRuleBo;
import com.bocoo.product.domain.bo.ProductPriceMaterialRuleBo;
import com.bocoo.product.domain.bo.ProductPriceQuoteBo;
import com.bocoo.product.domain.vo.ProductPriceQuoteVo;
import com.bocoo.product.domain.vo.ProductPriceSetupVo;
import com.bocoo.product.domain.vo.ProductPriceValidationIssueVo;
import com.bocoo.product.service.ProductPriceSettingService;
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
    @Log(title = "生成物料价格", businessType = BusinessType.UPDATE)
    @PutMapping("/{saleProductId}/generate-material-prices/{overwrite}")
    @Operation(summary = "按配方版本生成物料价格")
    public R<Void> generateMaterialPrices(@PathVariable Long saleProductId, @PathVariable Boolean overwrite) {
        return toAjax(priceSettingService.generateMaterialPrices(saleProductId, overwrite));
    }

    @SaCheckPermission("product:pricing:edit")
    @Log(title = "保存物料价格规则", businessType = BusinessType.UPDATE)
    @PutMapping("/{saleProductId}/materials/{priceMaterialId}/rules")
    @Operation(summary = "保存物料价格规则")
    public R<Void> saveMaterialRules(@PathVariable Long saleProductId, @PathVariable Long priceMaterialId,
                                   @RequestBody List<ProductPriceMaterialRuleBo> rules) {
        return toAjax(priceSettingService.saveMaterialRules(saleProductId, priceMaterialId, rules));
    }

    @SaCheckPermission("product:pricing:edit")
    @Log(title = "批量保存物料价格规则", businessType = BusinessType.UPDATE)
    @PutMapping("/{saleProductId}/materials/rules/batch")
    @Operation(summary = "批量保存物料价格规则")
    public R<Void> saveMaterialRulesBatch(@PathVariable Long saleProductId,
                                          @RequestBody ProductPriceMaterialBatchRuleBo batch) {
        return toAjax(priceSettingService.saveMaterialRulesBatch(saleProductId, batch));
    }

    @SaCheckPermission("product:pricing:validate")
    @Log(title = "校验产品价格", businessType = BusinessType.UPDATE)
    @PutMapping("/{saleProductId}/validate")
    @Operation(summary = "校验产品价格")
    public R<List<ProductPriceValidationIssueVo>> validatePrice(@PathVariable Long saleProductId) {
        return R.ok(priceSettingService.validatePrice(saleProductId));
    }

    @SaCheckPermission("product:pricing:query")
    @PostMapping("/{saleProductId}/quote")
    @Operation(summary = "按当前价格版本试算产品价格")
    public R<ProductPriceQuoteVo> quote(@PathVariable Long saleProductId, @RequestBody ProductPriceQuoteBo quote) {
        return R.ok(priceSettingService.quote(saleProductId, quote));
    }
}
