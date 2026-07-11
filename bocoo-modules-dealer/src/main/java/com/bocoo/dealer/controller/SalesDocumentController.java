package com.bocoo.dealer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.bo.SalesDocumentItemBo;
import com.bocoo.dealer.domain.vo.SalesDocumentItemVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.domain.vo.SalesProductSetupVo;
import com.bocoo.dealer.service.SalesDocumentDraftService;
import com.bocoo.dealer.service.SalesDocumentQueryService;
import com.bocoo.dealer.service.impl.SalesCatalogFacade;
import com.bocoo.product.domain.vo.ProductSaleProductVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/sales-documents")
public class SalesDocumentController extends BaseController {
    private final SalesDocumentQueryService queryService;
    private final SalesDocumentDraftService draftService;
    private final SalesCatalogFacade catalog;

    @SaCheckPermission("dealer:sales:list")
    @GetMapping("/list")
    public TableDataInfo<SalesDocumentVo> list(SalesDocumentBo bo, PageQuery pageQuery) { return queryService.queryPage(bo, pageQuery); }

    @SaCheckPermission("dealer:sales:query")
    @GetMapping("/{id:\\d+}")
    public R<SalesDocumentVo> get(@PathVariable Long id) { return R.ok(queryService.queryById(id)); }

    @SaCheckPermission("dealer:sales:query")
    @GetMapping("/customer/{customerId:\\d+}")
    public R<List<SalesDocumentVo>> customerHistory(@PathVariable Long customerId) { return R.ok(queryService.customerHistory(customerId)); }

    @SaCheckPermission("dealer:sales:edit")
    @GetMapping("/catalog/options")
    public R<List<ProductSaleProductVo>> options() { return R.ok(catalog.options()); }

    @SaCheckPermission("dealer:sales:edit")
    @GetMapping("/catalog/{productId:\\d+}/setup")
    public R<SalesProductSetupVo> setup(@PathVariable Long productId) { return R.ok(catalog.publicSetup(productId)); }

    @SaCheckPermission("dealer:sales:add")
    @Log(title = "新增销售单", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Long> add(@RequestBody SalesDocumentBo bo) { return R.ok(draftService.insert(bo)); }

    @SaCheckPermission("dealer:sales:edit")
    @PutMapping
    public R<Void> update(@RequestBody SalesDocumentBo bo) { return toAjax(draftService.update(bo)); }

    @SaCheckPermission("dealer:sales:add")
    @PostMapping("/{id:\\d+}/copy")
    public R<Long> copy(@PathVariable Long id) { return R.ok(draftService.copy(id)); }

    @SaCheckPermission("dealer:sales:remove")
    @DeleteMapping("/{ids:\\d+(?:,\\d+)*}")
    public R<Void> remove(@PathVariable Long[] ids) { return toAjax(draftService.delete(ids)); }

    @SaCheckPermission("dealer:sales:edit")
    @PostMapping("/calculate-item")
    public R<SalesDocumentItemVo> calculateItem(@RequestBody SalesDocumentItemBo bo) { return R.ok(draftService.calculateItem(bo)); }

    @SaCheckPermission("dealer:sales:edit")
    @PutMapping("/{id:\\d+}/calculate")
    public R<SalesDocumentVo> calculateAll(@PathVariable Long id) { return R.ok(draftService.calculateAll(id)); }
}
