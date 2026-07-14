package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.validate.AddGroup;
import com.bocoo.common.core.validate.EditGroup;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.SalesStoreBo;
import com.bocoo.system.domain.vo.SalesStoreDeptOptionVo;
import com.bocoo.system.domain.vo.SalesStoreReferenceVo;
import com.bocoo.system.domain.vo.SalesStoreOptionVo;
import com.bocoo.system.domain.vo.SalesStoreVo;
import com.bocoo.system.service.SalesStoreManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/sales-store")
@Tag(name = "Sales store management")
public class SalesStoreController extends BaseController {

    private final SalesStoreManagementService salesStoreService;

    @SaCheckPermission("system:sales-store:list")
    @GetMapping("/list")
    @Operation(summary = "Query sales store page")
    public TableDataInfo<SalesStoreVo> list(SalesStoreBo bo, PageQuery pageQuery) {
        return salesStoreService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("system:sales-store:options")
    @GetMapping("/options")
    @Operation(summary = "Query enabled sales store options")
    public R<List<SalesStoreOptionVo>> options() {
        return R.ok(salesStoreService.queryOptions());
    }

    @SaCheckPermission("system:sales-store:list")
    @GetMapping("/dept-options")
    @Operation(summary = "Query platform department options")
    public R<List<SalesStoreDeptOptionVo>> deptOptions() {
        return R.ok(salesStoreService.queryDeptOptions());
    }

    @SaCheckPermission("system:sales-store:query")
    @GetMapping("/{salesStoreId}")
    @Operation(summary = "Query sales store detail")
    public R<SalesStoreVo> getInfo(@NotNull @PathVariable Long salesStoreId) {
        return R.ok(salesStoreService.queryManagementById(salesStoreId));
    }

    @SaCheckPermission("system:sales-store:add")
    @Log(title = "Sales store", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "Create sales store")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody SalesStoreBo bo) {
        return toAjax(salesStoreService.insertByBo(bo));
    }

    @SaCheckPermission("system:sales-store:edit")
    @Log(title = "Sales store", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "Update sales store")
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody SalesStoreBo bo) {
        return toAjax(salesStoreService.updateByBo(bo));
    }

    @SaCheckPermission("system:sales-store:status")
    @GetMapping("/{salesStoreId}/disable-check")
    @Operation(summary = "Check references before disabling sales store")
    public R<SalesStoreReferenceVo> disableCheck(@PathVariable Long salesStoreId) {
        return R.ok(salesStoreService.checkDisableReferences(salesStoreId));
    }

    @SaCheckPermission("system:sales-store:status")
    @Log(title = "Sales store status", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{salesStoreId}/{status}")
    @Operation(summary = "Change sales store status")
    public R<Void> changeStatus(@PathVariable Long salesStoreId, @PathVariable String status) {
        return toAjax(salesStoreService.updateStatus(salesStoreId, status));
    }
}
