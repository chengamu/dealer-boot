package com.bocoo.dealer.quickorder.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.dealer.quickorder.domain.bo.QuickOrderBo;
import com.bocoo.dealer.quickorder.domain.vo.QuickOrderVo;
import com.bocoo.dealer.quickorder.service.QuickOrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/quick-orders")
public class QuickOrderQueryController extends BaseController {
    private final QuickOrderQueryService service;

    @SaCheckPermission("dealer:quick-order:list")
    @GetMapping("/list")
    public TableDataInfo<QuickOrderVo> list(QuickOrderBo bo, PageQuery pageQuery) {
        return service.queryPage(bo, pageQuery);
    }

    @SaCheckPermission("dealer:quick-order:query")
    @GetMapping("/{id:\\d+}")
    public R<QuickOrderVo> get(@PathVariable Long id) {
        return R.ok(service.queryById(id));
    }
}
