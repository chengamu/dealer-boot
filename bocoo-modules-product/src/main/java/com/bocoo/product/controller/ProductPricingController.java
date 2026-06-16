package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.PriceCalculationBo;
import com.bocoo.product.domain.bo.PricePlanBo;
import com.bocoo.product.domain.bo.PricePlanVersionBo;
import com.bocoo.product.domain.bo.PriceRuleItemBo;
import com.bocoo.product.domain.vo.PriceCalculationResultVo;
import com.bocoo.product.domain.vo.PricePlanVersionVo;
import com.bocoo.product.domain.vo.PricePlanVo;
import com.bocoo.product.domain.vo.PriceRuleItemVo;
import com.bocoo.product.service.PriceCalculationService;
import com.bocoo.product.service.PricePlanService;
import com.bocoo.product.service.PricePlanVersionService;
import com.bocoo.product.service.PriceRuleItemService;
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

/**
 * 产品价格中心接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品价格中心", description = "产品价格中心接口")
public class ProductPricingController extends BaseController {

    private final PricePlanService pricePlanService;
    private final PricePlanVersionService pricePlanVersionService;
    private final PriceRuleItemService priceRuleItemService;
    private final PriceCalculationService priceCalculationService;

    @SaCheckPermission("product:price:list")
    @GetMapping("/price-plans/list")
    @Operation(summary = "分页查询价格方案列表")
    public TableDataInfo<PricePlanVo> listPricePlan(PricePlanBo bo, PageQuery pageQuery) {
        return pricePlanService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:price:list")
    @GetMapping("/price-plans/options")
    @Operation(summary = "查询价格方案选项")
    public R<java.util.List<PricePlanVo>> optionsPricePlan(PricePlanBo bo) {
        return R.ok(pricePlanService.queryList(bo));
    }

    @SaCheckPermission("product:price:list")
    @GetMapping("/price-plans/{id}")
    @Operation(summary = "获取价格方案详情")
    public R<PricePlanVo> getPricePlan(@PathVariable Long id) {
        return R.ok(pricePlanService.queryById(id));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格方案", businessType = BusinessType.INSERT)
    @PostMapping("/price-plans")
    @Operation(summary = "新增价格方案")
    public R<Void> addPricePlan(@Validated @RequestBody PricePlanBo bo) {
        return toAjax(pricePlanService.insertByBo(bo));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格方案", businessType = BusinessType.UPDATE)
    @PutMapping("/price-plans")
    @Operation(summary = "修改价格方案")
    public R<Void> editPricePlan(@Validated @RequestBody PricePlanBo bo) {
        return toAjax(pricePlanService.updateByBo(bo));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格方案", businessType = BusinessType.DELETE)
    @DeleteMapping("/price-plans/{ids}")
    @Operation(summary = "删除价格方案")
    public R<Void> removePricePlan(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(pricePlanService.deleteWithValidByIds(ids));
    }


    @SaCheckPermission("product:price:list")
    @GetMapping("/price-plan-versions/list")
    @Operation(summary = "分页查询价格方案版本列表")
    public TableDataInfo<PricePlanVersionVo> listPricePlanVersion(PricePlanVersionBo bo, PageQuery pageQuery) {
        return pricePlanVersionService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:price:list")
    @GetMapping("/price-plan-versions/options")
    @Operation(summary = "查询价格方案版本选项")
    public R<java.util.List<PricePlanVersionVo>> optionsPricePlanVersion(PricePlanVersionBo bo) {
        return R.ok(pricePlanVersionService.queryList(bo));
    }

    @SaCheckPermission("product:price:list")
    @GetMapping("/price-plan-versions/{id}")
    @Operation(summary = "获取价格方案版本详情")
    public R<PricePlanVersionVo> getPricePlanVersion(@PathVariable Long id) {
        return R.ok(pricePlanVersionService.queryById(id));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格方案版本", businessType = BusinessType.INSERT)
    @PostMapping("/price-plan-versions")
    @Operation(summary = "新增价格方案版本")
    public R<Void> addPricePlanVersion(@Validated @RequestBody PricePlanVersionBo bo) {
        return toAjax(pricePlanVersionService.insertByBo(bo));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格方案版本", businessType = BusinessType.UPDATE)
    @PutMapping("/price-plan-versions")
    @Operation(summary = "修改价格方案版本")
    public R<Void> editPricePlanVersion(@Validated @RequestBody PricePlanVersionBo bo) {
        return toAjax(pricePlanVersionService.updateByBo(bo));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格方案版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/price-plan-versions/{ids}")
    @Operation(summary = "删除价格方案版本")
    public R<Void> removePricePlanVersion(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(pricePlanVersionService.deleteWithValidByIds(ids));
    }


    @SaCheckPermission("product:price:list")
    @GetMapping("/price-rule-items/list")
    @Operation(summary = "分页查询价格规则项列表")
    public TableDataInfo<PriceRuleItemVo> listPriceRuleItem(PriceRuleItemBo bo, PageQuery pageQuery) {
        return priceRuleItemService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:price:list")
    @GetMapping("/price-rule-items/options")
    @Operation(summary = "查询价格规则项选项")
    public R<java.util.List<PriceRuleItemVo>> optionsPriceRuleItem(PriceRuleItemBo bo) {
        return R.ok(priceRuleItemService.queryList(bo));
    }

    @SaCheckPermission("product:price:list")
    @GetMapping("/price-rule-items/{id}")
    @Operation(summary = "获取价格规则项详情")
    public R<PriceRuleItemVo> getPriceRuleItem(@PathVariable Long id) {
        return R.ok(priceRuleItemService.queryById(id));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格规则项", businessType = BusinessType.INSERT)
    @PostMapping("/price-rule-items")
    @Operation(summary = "新增价格规则项")
    public R<Void> addPriceRuleItem(@Validated @RequestBody PriceRuleItemBo bo) {
        return toAjax(priceRuleItemService.insertByBo(bo));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格规则项", businessType = BusinessType.UPDATE)
    @PutMapping("/price-rule-items")
    @Operation(summary = "修改价格规则项")
    public R<Void> editPriceRuleItem(@Validated @RequestBody PriceRuleItemBo bo) {
        return toAjax(priceRuleItemService.updateByBo(bo));
    }

    @SaCheckPermission("product:price:edit")
    @Log(title = "价格规则项", businessType = BusinessType.DELETE)
    @DeleteMapping("/price-rule-items/{ids}")
    @Operation(summary = "删除价格规则项")
    public R<Void> removePriceRuleItem(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(priceRuleItemService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:price:test")
    @PostMapping("/pricing/calculate")
    @Operation(summary = "产品价格试算")
    public R<PriceCalculationResultVo> calculate(@RequestBody PriceCalculationBo bo) {
        return R.ok(priceCalculationService.calculate(bo));
    }
}
