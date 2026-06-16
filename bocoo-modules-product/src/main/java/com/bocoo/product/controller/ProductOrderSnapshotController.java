package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.OrderSnapshotBuildBo;
import com.bocoo.product.domain.entity.ProductSnapshotInstance;
import com.bocoo.product.domain.vo.OrderProductSnapshotVo;
import com.bocoo.product.domain.vo.ProductSnapshotInstanceVo;
import com.bocoo.product.service.ProductOrderSnapshotService;
import com.bocoo.product.service.ProductSnapshotInstanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品能力快照实例接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品能力快照实例", description = "产品能力快照实例接口")
public class ProductOrderSnapshotController {

    private final ProductOrderSnapshotService productOrderSnapshotService;
    private final ProductSnapshotInstanceService productSnapshotInstanceService;

    @SaCheckPermission("product:order-snapshot:build")
    @PostMapping("/order-snapshots/build")
    @Operation(summary = "构建订单产品快照")
    public R<OrderProductSnapshotVo> build(@RequestBody OrderSnapshotBuildBo bo) {
        return R.ok(productOrderSnapshotService.buildSnapshot(bo));
    }

    @SaCheckPermission("product:snapshot-instance:build")
    @PostMapping("/snapshot-instances/build")
    @Operation(summary = "构建并保存产品能力快照实例")
    public R<OrderProductSnapshotVo> buildAndSave(@RequestBody OrderSnapshotBuildBo bo) {
        return R.ok(productOrderSnapshotService.buildAndSaveSnapshot(bo));
    }

    @SaCheckPermission("product:snapshot-instance:list")
    @GetMapping("/snapshot-instances/list")
    @Operation(summary = "分页查询产品能力快照实例")
    public TableDataInfo<ProductSnapshotInstanceVo> list(ProductSnapshotInstance query, PageQuery pageQuery) {
        return productSnapshotInstanceService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:snapshot-instance:query")
    @GetMapping("/snapshot-instances/{snapshotId}")
    @Operation(summary = "获取产品能力快照实例详情")
    public R<ProductSnapshotInstanceVo> get(@Parameter(description = "产品能力快照实例ID", required = true) @PathVariable Long snapshotId) {
        return R.ok(productSnapshotInstanceService.queryById(snapshotId));
    }
}
