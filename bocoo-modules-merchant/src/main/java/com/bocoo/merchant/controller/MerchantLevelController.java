package com.bocoo.merchant.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.merchant.domain.bo.MerchantLevelBo;
import com.bocoo.merchant.domain.vo.MerchantLevelVo;
import com.bocoo.merchant.service.MerchantLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/merchant/levels")
@Tag(name = "商户等级", description = "商户等级增删改查、状态和导出接口")
public class MerchantLevelController extends BaseController {

    private final MerchantLevelService merchantLevelService;

    @SaCheckPermission("merchant:level:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询商户等级")
    public TableDataInfo<MerchantLevelVo> list(MerchantLevelBo bo, PageQuery pageQuery) {
        return merchantLevelService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("merchant:level:list")
    @GetMapping("/options")
    @Operation(summary = "查询商户等级选项")
    public R<List<MerchantLevelVo>> options(MerchantLevelBo bo) {
        return R.ok(merchantLevelService.queryList(bo));
    }

    @Log(title = "商户等级", businessType = BusinessType.EXPORT)
    @SaCheckPermission("merchant:level:export")
    @PostMapping("/export")
    @Operation(summary = "导出商户等级")
    public void export(MerchantLevelBo bo, HttpServletResponse response) {
        ExcelUtil.exportExcel(merchantLevelService.queryList(bo), "商户等级", MerchantLevelVo.class, response);
    }

    @SaCheckPermission("merchant:level:query")
    @GetMapping("/{id}")
    @Operation(summary = "获取商户等级详情")
    public R<MerchantLevelVo> get(@PathVariable Long id) {
        return R.ok(merchantLevelService.queryById(id));
    }

    @SaCheckPermission("merchant:level:add")
    @Log(title = "商户等级", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增商户等级")
    public R<Void> add(@Validated @RequestBody MerchantLevelBo bo) {
        return toAjax(merchantLevelService.insertByBo(bo));
    }

    @SaCheckPermission("merchant:level:edit")
    @Log(title = "商户等级", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改商户等级")
    public R<Void> edit(@Validated @RequestBody MerchantLevelBo bo) {
        return toAjax(merchantLevelService.updateByBo(bo));
    }

    @SaCheckPermission("merchant:level:edit")
    @Log(title = "修改商户等级状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{id}/{status}")
    @Operation(summary = "修改商户等级状态")
    public R<Void> changeStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(merchantLevelService.updateStatus(id, status));
    }

    @SaCheckPermission("merchant:level:remove")
    @Log(title = "商户等级", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除商户等级")
    public R<Void> remove(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(merchantLevelService.deleteWithValidByIds(ids));
    }
}
