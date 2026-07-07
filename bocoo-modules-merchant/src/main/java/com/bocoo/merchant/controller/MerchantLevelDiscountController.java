package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.merchant.domain.bo.MerchantLevelDiscountBo;
import com.bocoo.merchant.domain.vo.MerchantLevelDiscountVo;
import com.bocoo.merchant.service.MerchantLevelDiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/merchant/level-discounts")
@Tag(name = "商户等级产品折扣", description = "商户等级产品折扣增删改查、状态和导出接口")
public class MerchantLevelDiscountController extends BaseController {

    private final MerchantLevelDiscountService discountService;

    @SaCheckPermission("merchant:levelDiscount:list")
    @GetMapping("/list")
    public TableDataInfo<MerchantLevelDiscountVo> list(MerchantLevelDiscountBo bo, PageQuery pageQuery) {
        return discountService.queryPageList(bo, pageQuery);
    }

    @Log(title = "商户等级产品折扣", businessType = BusinessType.EXPORT)
    @SaCheckPermission("merchant:levelDiscount:export")
    @PostMapping("/export")
    public void export(MerchantLevelDiscountBo bo, HttpServletResponse response) {
        ExcelUtil.exportExcel(discountService.queryList(bo), "等级产品折扣", MerchantLevelDiscountVo.class, response);
    }

    @SaCheckPermission("merchant:levelDiscount:query")
    @GetMapping("/{id}")
    public R<MerchantLevelDiscountVo> get(@PathVariable Long id) {
        return R.ok(discountService.queryById(id));
    }

    @SaCheckPermission("merchant:levelDiscount:add")
    @Log(title = "商户等级产品折扣", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody MerchantLevelDiscountBo bo) {
        return toAjax(discountService.insertByBo(bo));
    }

    @SaCheckPermission("merchant:levelDiscount:edit")
    @Log(title = "商户等级产品折扣", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody MerchantLevelDiscountBo bo) {
        return toAjax(discountService.updateByBo(bo));
    }

    @SaCheckPermission("merchant:levelDiscount:edit")
    @Log(title = "修改等级产品折扣状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(discountService.updateStatus(id, status));
    }

    @SaCheckPermission("merchant:levelDiscount:remove")
    @Log(title = "商户等级产品折扣", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(discountService.deleteWithValidByIds(ids));
    }
}
