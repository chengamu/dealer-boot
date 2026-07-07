package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.merchant.domain.bo.CustomerProfileBo;
import com.bocoo.merchant.domain.vo.CustomerOwnerOptionVo;
import com.bocoo.merchant.domain.vo.CustomerProfileVo;
import com.bocoo.merchant.service.CustomerProfileService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/customer/customers")
public class CustomerProfileController extends BaseController {

    private final CustomerProfileService customerProfileService;

    @SaCheckPermission("customer:profile:list")
    @GetMapping("/list")
    public TableDataInfo<CustomerProfileVo> list(CustomerProfileBo bo, PageQuery pageQuery) {
        return customerProfileService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("customer:profile:list")
    @GetMapping("/options")
    public R<List<CustomerProfileVo>> options(CustomerProfileBo bo) {
        return R.ok(customerProfileService.queryOptions(bo));
    }

    @SaCheckPermission("customer:profile:list")
    @GetMapping("/owner-options")
    public R<List<CustomerOwnerOptionVo>> ownerOptions() {
        return R.ok(customerProfileService.queryOwnerOptions(null));
    }

    @SaCheckPermission("customer:profile:query")
    @GetMapping("/{id:\\d+}")
    public R<CustomerProfileVo> get(@PathVariable Long id) {
        return R.ok(customerProfileService.queryById(id));
    }

    @SaCheckPermission("customer:profile:add")
    @Log(title = "客户资料", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody CustomerProfileBo bo) {
        return toAjax(customerProfileService.insertByBo(bo));
    }

    @SaCheckPermission("customer:profile:edit")
    @Log(title = "客户资料", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody CustomerProfileBo bo) {
        return toAjax(customerProfileService.updateByBo(bo));
    }

    @SaCheckPermission("customer:profile:edit")
    @Log(title = "修改客户状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(customerProfileService.updateStatus(id, status));
    }

    @SaCheckPermission("customer:profile:remove")
    @Log(title = "客户资料", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids:\\d+(?:,\\d+)*}")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(customerProfileService.deleteWithValidByIds(ids));
    }
}
