package com.bocoo.demo.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.validate.AddGroup;
import com.bocoo.common.core.validate.EditGroup;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.demo.domain.bo.DemoTechManagementBo;
import com.bocoo.demo.domain.vo.DemoTechManagementVo;
import com.bocoo.demo.domain.vo.DemoTechnologyStatusCountVo;
import com.bocoo.demo.service.DemoTechManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 技术管理示例控制层。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/demo/tech-management")
@Tag(name = "技术管理示例", description = "后端标准 CRUD 示例接口")
public class DemoTechManagementController extends BaseController {

    private final DemoTechManagementService techManagementService;

    @SaCheckPermission("demo:tech:list")
    @GetMapping("/list")
    @Operation(summary = "分页查询技术管理示例列表")
    public TableDataInfo<DemoTechManagementVo> list(
        @Parameter(description = "技术管理示例查询参数") DemoTechManagementBo bo,
        @Parameter(description = "分页参数") PageQuery pageQuery) {
        return techManagementService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("demo:tech:list")
    @GetMapping("/options")
    @Operation(summary = "查询技术管理示例选项")
    public R<List<DemoTechManagementVo>> options(DemoTechManagementBo bo) {
        return R.ok(techManagementService.queryList(bo));
    }

    @SaCheckPermission("demo:tech:query")
    @GetMapping("/{managementId}")
    @Operation(summary = "获取技术管理示例详情")
    public R<DemoTechManagementVo> getInfo(
        @Parameter(description = "技术管理示例主键", required = true)
        @NotNull(message = "{gen.validation.id.required}")
        @PathVariable Long managementId) {
        return R.ok(techManagementService.queryById(managementId));
    }

    @SaCheckPermission("demo:tech:add")
    @Log(title = "技术管理示例", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增技术管理示例")
    public R<Void> add(@Validated(AddGroup.class) @RequestBody DemoTechManagementBo bo) {
        return toAjax(techManagementService.insertByBo(bo));
    }

    @SaCheckPermission("demo:tech:edit")
    @Log(title = "技术管理示例", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改技术管理示例")
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody DemoTechManagementBo bo) {
        return toAjax(techManagementService.updateByBo(bo));
    }

    @SaCheckPermission("demo:tech:remove")
    @Log(title = "技术管理示例", businessType = BusinessType.DELETE)
    @DeleteMapping("/{managementIds}")
    @Operation(summary = "批量删除技术管理示例")
    public R<Void> remove(
        @Parameter(description = "技术管理示例主键数组", required = true)
        @NotEmpty(message = "{gen.validation.pk.required}")
        @PathVariable Long[] managementIds) {
        return toAjax(techManagementService.deleteWithValidByIds(List.of(managementIds), true));
    }

    @SaCheckPermission("demo:tech:edit")
    @Log(title = "修改技术管理示例状态", businessType = BusinessType.UPDATE)
    @PutMapping("/change-status/{managementId}/{status}")
    @Operation(summary = "修改技术管理示例状态")
    public R<Void> changeStatus(
        @Parameter(description = "技术管理示例主键", required = true)
        @NotNull(message = "{gen.validation.id.required}")
        @PathVariable Long managementId,
        @Parameter(description = "技术管理示例状态", required = true)
        @PathVariable String status) {
        return toAjax(techManagementService.updateStatus(managementId, status));
    }

    @SaCheckPermission("demo:tech:list")
    @GetMapping("/status-count")
    @Operation(summary = "按状态统计技术管理示例数量")
    public R<List<DemoTechnologyStatusCountVo>> statusCount() {
        return R.ok(techManagementService.selectTechnologyStatusCount());
    }
}
