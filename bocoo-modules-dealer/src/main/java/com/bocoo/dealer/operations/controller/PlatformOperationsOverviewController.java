package com.bocoo.dealer.operations.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.dealer.operations.domain.bo.OperationsMerchantQueryBo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantLevelOptionVo;
import com.bocoo.dealer.operations.domain.vo.OperationsMerchantVo;
import com.bocoo.dealer.operations.domain.vo.OperationsSummaryVo;
import com.bocoo.dealer.operations.service.PlatformOperationsOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/platform-sales/operations")
public class PlatformOperationsOverviewController {

    private final PlatformOperationsOverviewService service;

    @SaCheckPermission("platform:sales:dashboard:view")
    @GetMapping("/summary")
    public R<OperationsSummaryVo> summary() {
        return R.ok(service.summary());
    }

    @SaCheckPermission("platform:sales:dashboard:view")
    @GetMapping("/merchants")
    public TableDataInfo<OperationsMerchantVo> merchants(OperationsMerchantQueryBo query, PageQuery pageQuery) {
        return service.merchantPage(query, pageQuery);
    }

    @SaCheckPermission("platform:sales:dashboard:view")
    @GetMapping("/levels/options")
    public R<java.util.List<OperationsMerchantLevelOptionVo>> levels(String status) {
        return R.ok(service.merchantLevelOptions(status));
    }

    @SaCheckPermission("platform:sales:dashboard:view")
    @GetMapping("/applications")
    public TableDataInfo<OperationsMerchantVo> applications(OperationsMerchantQueryBo query, PageQuery pageQuery) {
        return service.pendingMerchantApplications(query, pageQuery);
    }
}
