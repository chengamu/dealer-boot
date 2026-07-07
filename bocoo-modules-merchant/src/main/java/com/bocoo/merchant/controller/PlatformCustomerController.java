package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.vo.CustomerOwnerOptionVo;
import com.bocoo.merchant.domain.vo.CustomerProfileVo;
import com.bocoo.merchant.service.CustomerProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/platform/customers")
public class PlatformCustomerController extends BaseController {

    private final CustomerProfileService customerProfileService;

    @SaCheckPermission("platform:customer:list")
    @GetMapping("/list")
    public TableDataInfo<CustomerProfileVo> list(CustomerProfileBo bo, PageQuery pageQuery) {
        return customerProfileService.queryPlatformPageList(bo, pageQuery);
    }

    @SaCheckPermission("platform:customer:list")
    @GetMapping("/owner-options")
    public R<List<CustomerOwnerOptionVo>> ownerOptions(@RequestParam Long tenantId) {
        return R.ok(customerProfileService.queryOwnerOptions(tenantId));
    }

    @SaCheckPermission("platform:customer:query")
    @GetMapping("/{id:\\d+}")
    public R<CustomerProfileVo> get(@PathVariable Long id) {
        return R.ok(customerProfileService.queryPlatformById(id));
    }
}
