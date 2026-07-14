package com.bocoo.dealer.quickorder.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.domain.vo.PlatformQuickOrderExportVo;
import com.bocoo.dealer.quickorder.service.PlatformQuickOrderQueryService;
import com.bocoo.common.excel.utils.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform-sales/quick-orders")
public class PlatformQuickOrderQueryController {
    private final PlatformQuickOrderQueryService service;

    @SaCheckPermission("platform:sales:quick-order:list")
    @GetMapping("/list")
    public TableDataInfo<QuickOrderVo> list(QuickOrderBo bo, PageQuery pageQuery) {
        return service.queryPage(bo, pageQuery);
    }

    @SaCheckPermission("platform:sales:quick-order:query")
    @GetMapping("/{id:\\d+}")
    public R<QuickOrderVo> get(@PathVariable Long id) {
        return R.ok(service.queryById(id));
    }

    @SaCheckPermission("platform:sales:quick-order:export")
    @org.springframework.web.bind.annotation.PostMapping("/export")
    public void export(QuickOrderBo bo, HttpServletResponse response) {
        ExcelUtil.exportExcel(service.export(bo), "Quick Orders", PlatformQuickOrderExportVo.class, response);
    }
}
