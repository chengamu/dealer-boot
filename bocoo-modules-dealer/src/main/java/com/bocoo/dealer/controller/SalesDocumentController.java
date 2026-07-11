package com.bocoo.dealer.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.domain.bo.SalesDocumentBo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.service.SalesDocumentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/sales-documents")
public class SalesDocumentController extends BaseController {
    private final SalesDocumentQueryService queryService;

    @SaCheckPermission("dealer:sales:list")
    @GetMapping("/list")
    public TableDataInfo<SalesDocumentVo> list(SalesDocumentBo bo, PageQuery pageQuery) { return queryService.queryPage(bo, pageQuery); }

    @SaCheckPermission("dealer:sales:query")
    @GetMapping("/{id:\\d+}")
    public R<SalesDocumentVo> get(@PathVariable Long id) { return R.ok(queryService.queryById(id)); }

    @SaCheckPermission("dealer:sales:query")
    @GetMapping("/customer/{customerId:\\d+}")
    public R<List<SalesDocumentVo>> customerHistory(@PathVariable Long customerId) { return R.ok(queryService.customerHistory(customerId)); }

}
