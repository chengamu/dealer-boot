package com.bocoo.pay.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.pay.domain.bo.BankTransferQueryBo;
import com.bocoo.pay.domain.bo.PayBankReviewBo;
import com.bocoo.pay.domain.vo.BankTransferSummaryVo;
import com.bocoo.pay.domain.vo.PayAttemptVo;
import com.bocoo.pay.service.PlatformBankTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform-finance/bank-transfers")
public class PlatformBankTransferController {
    private final PlatformBankTransferService service;

    @SaCheckPermission("platform:finance:bank:list")
    @GetMapping("/list")
    public TableDataInfo<BankTransferSummaryVo> list(BankTransferQueryBo query, PageQuery pageQuery) {
        return service.list(query, pageQuery);
    }

    @SaCheckPermission("platform:finance:bank:query")
    @GetMapping("/{extensionId}")
    public R<BankTransferSummaryVo> detail(@PathVariable Long extensionId) {
        return R.ok(service.detail(extensionId));
    }

    @SaCheckPermission("platform:finance:bank:review")
    @PostMapping("/{extensionId}/review")
    public R<PayAttemptVo> review(@PathVariable Long extensionId, @Valid @RequestBody PayBankReviewBo bo) {
        return R.ok(service.review(extensionId, bo));
    }
}
