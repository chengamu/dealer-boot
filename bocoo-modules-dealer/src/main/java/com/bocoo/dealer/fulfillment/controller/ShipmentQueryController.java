package com.bocoo.dealer.fulfillment.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.fulfillment.domain.bo.ShipmentQueryBo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentOrderVo;
import com.bocoo.dealer.fulfillment.domain.vo.ShipmentVo;
import com.bocoo.dealer.fulfillment.service.ShipmentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/fulfillment")
public class ShipmentQueryController extends BaseController {
    private final ShipmentQueryService service;

    @SaCheckPermission("dealer:fulfillment:shipment:list")
    @GetMapping("/shipment/list")
    public TableDataInfo<ShipmentOrderVo> list(ShipmentQueryBo bo, PageQuery pageQuery) {
        return service.queryPage(bo, pageQuery);
    }

    @SaCheckPermission("dealer:fulfillment:shipment:query")
    @GetMapping("/orders/{id:\\d+}/shipments")
    public R<List<ShipmentVo>> orderShipments(@PathVariable Long id) {
        return R.ok(service.orderShipments(id));
    }

    @SaCheckPermission("dealer:fulfillment:shipment:query")
    @GetMapping("/shipments/{id:\\d+}")
    public R<ShipmentVo> detail(@PathVariable Long id) {
        return R.ok(service.detail(id));
    }
}
