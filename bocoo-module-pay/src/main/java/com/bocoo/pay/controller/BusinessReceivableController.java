package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.ReceivableQueryBo;
import com.bocoo.pay.domain.vo.PayReceivableSummaryVo;
import com.bocoo.pay.service.BusinessCreditQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dealer/receivables")
public class BusinessReceivableController {
    private final BusinessCreditQueryService service;

    @SaCheckPermission("pay:receivable:list")
    @GetMapping("/list")
    public TableDataInfo<PayReceivableSummaryVo> list(ReceivableQueryBo query, PageQuery pageQuery) {
        return service.receivables(query, pageQuery);
    }

    @SaCheckPermission("pay:receivable:query")
    @GetMapping("/{receivableId}")
    public R<PayReceivableSummaryVo> detail(@PathVariable Long receivableId) {
        return R.ok(service.receivable(receivableId));
    }
}
