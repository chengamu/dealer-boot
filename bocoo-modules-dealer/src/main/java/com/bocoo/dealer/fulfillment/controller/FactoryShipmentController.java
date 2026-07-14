package com.bocoo.dealer.fulfillment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentBo;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.FulfillmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;
import com.bocoo.dealer.fulfillment.service.FactoryShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/fulfillment/factory/shipment")
public class FactoryShipmentController extends BaseController {
    private final FactoryShipmentService service;

    @SaCheckPermission("dealer:fulfillment:factory:shipment:list")
    @GetMapping("/list")
    public TableDataInfo<ShipmentOrderVo> list(ShipmentQueryBo bo, PageQuery pageQuery) {
        return service.queryPage(bo, pageQuery);
    }

    @SaCheckPermission("dealer:fulfillment:factory:shipment:query")
    @GetMapping("/orders/{id:\\d+}")
    public R<FulfillmentOrderVo> detail(@PathVariable Long id) { return R.ok(service.detail(id)); }

    @Log(title = "Shipment Draft", businessType = BusinessType.INSERT)
    @SaCheckPermission("dealer:fulfillment:factory:shipment:add")
    @PostMapping("/orders/{id:\\d+}/shipments")
    public R<ShipmentVo> create(@PathVariable Long id, @Valid @RequestBody ShipmentBo bo) {
        return R.ok(service.create(id, bo));
    }

    @Log(title = "Shipment Draft", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:fulfillment:factory:shipment:edit")
    @PutMapping("/shipments/{id:\\d+}")
    public R<Void> update(@PathVariable Long id, @Valid @RequestBody ShipmentBo bo) {
        return toAjax(service.update(id, bo));
    }

    @Log(title = "Shipment Draft", businessType = BusinessType.DELETE)
    @SaCheckPermission("dealer:fulfillment:factory:shipment:remove")
    @DeleteMapping("/shipments/{id:\\d+}")
    public R<Void> delete(@PathVariable Long id) { return toAjax(service.delete(id)); }

    @Log(title = "Shipment Dispatch", businessType = BusinessType.UPDATE)
    @SaCheckPermission("dealer:fulfillment:factory:shipment:dispatch")
    @PutMapping("/shipments/{id:\\d+}/dispatch")
    public R<Void> dispatch(@PathVariable Long id) { return toAjax(service.dispatch(id)); }
}
