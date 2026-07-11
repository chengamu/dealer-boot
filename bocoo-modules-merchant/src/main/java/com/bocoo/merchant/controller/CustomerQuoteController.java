package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.merchant.domain.bo.CustomerQuoteBo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.bocoo.merchant.service.CustomerQuoteDraftService;
import com.bocoo.merchant.service.CustomerQuoteQueryService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/customer/quotes")
public class CustomerQuoteController extends BaseController {

    private final CustomerQuoteQueryService queryService;
    private final CustomerQuoteDraftService draftService;

    @SaCheckPermission("customer:quote:list")
    @GetMapping("/list")
    public TableDataInfo<CustomerQuoteVo> list(CustomerQuoteBo bo, PageQuery pageQuery) {
        return queryService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("customer:quote:query")
    @GetMapping("/{id:\\d+}")
    public R<CustomerQuoteVo> get(@PathVariable Long id) {
        return R.ok(queryService.queryById(id));
    }

    @SaCheckPermission("customer:quote:add")
    @Log(title = "客户报价", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Long> add(@Validated @RequestBody CustomerQuoteBo bo) {
        return R.ok(draftService.insert(bo));
    }

    @SaCheckPermission("customer:quote:edit")
    @Log(title = "客户报价", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody CustomerQuoteBo bo) {
        return toAjax(draftService.update(bo));
    }

    @SaCheckPermission("customer:quote:add")
    @Log(title = "复制订单测算", businessType = BusinessType.INSERT)
    @PostMapping("/{id:\\d+}/copy")
    public R<Long> copy(@PathVariable Long id) {
        return R.ok(draftService.copy(id));
    }

    @SaCheckPermission("customer:quote:remove")
    @Log(title = "客户报价", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids:\\d+(?:,\\d+)*}")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(draftService.delete(ids));
    }
}
