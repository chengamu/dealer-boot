package com.bocoo.dealer.fulfillment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaMode;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.fulfillment.domain.bo.ProductionCompleteBo;
import com.bocoo.dealer.fulfillment.domain.bo.ProductionQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ProductionOrderVo;
import com.bocoo.dealer.fulfillment.service.ProductionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/fulfillment")
public class ProductionController extends BaseController {
    private final ProductionService service;

    @SaCheckPermission("dealer:fulfillment:production:list")
    @GetMapping("/production/list")
    public TableDataInfo<ProductionOrderVo> list(ProductionQueryBo bo, PageQuery pageQuery) {
        return service.queryPage(bo, pageQuery);
    }

    @SaCheckPermission(value = {"dealer:fulfillment:production:query", "dealer:fulfillment:shipment:query"}, mode = SaMode.OR)
    @GetMapping("/orders/{id:\\d+}")
    public R<FulfillmentOrderVo> detail(@PathVariable Long id) {
        return R.ok(service.detail(id));
    }

    @Log(title = "Production Start", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:fulfillment:production:start")
    @PutMapping("/orders/{id:\\d+}/production/start")
    public R<Void> start(@PathVariable Long id) {
        return toAjax(service.start(id));
    }

    @Log(title = "Production Complete", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:fulfillment:production:complete")
    @PutMapping("/orders/{id:\\d+}/production/complete")
    public R<Void> complete(@PathVariable Long id, @Valid @RequestBody ProductionCompleteBo bo) {
        return toAjax(service.complete(id, bo.getNote()));
    }
}
