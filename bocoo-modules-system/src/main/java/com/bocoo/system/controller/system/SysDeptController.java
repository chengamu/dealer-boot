package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.core.domain.R;
import com.bocoo.system.domain.bo.SysDeptBo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.system.domain.vo.SysDeptVo;
import com.bocoo.system.service.SysDeptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门信息
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dept")
@Tag(name = "部门管理", description = "部门信息管理接口")
public class SysDeptController extends BaseController {

    private final SysDeptService deptService;

    /**
     * 获取部门列表
     */
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list")
    @Operation(summary = "获取部门列表", description = "获取部门列表")
    public R<List<SysDeptVo>> list(
            @Parameter(description = "部门查询参数")
            SysDeptBo dept) {
        List<SysDeptVo> depts = deptService.selectDeptList(dept);
        return R.ok(depts);
    }


    @GetMapping("/allList")
    @Operation(summary = "获取部门列表", description = "获取部门列表")
    public R<List<SysDeptVo>> allList(
            @Parameter(description = "部门查询参数")
            SysDeptBo dept) {
        List<SysDeptVo> depts = deptService.selectDeptList(dept);
        return R.ok(depts);
    }


    /**
     * 查询部门列表（排除节点）
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:list")
    @GetMapping("/list/exclude/{deptId}")
    @Operation(summary = "查询部门列表（排除节点）", description = "查询部门列表，排除指定节点及其子节点")
    public R<List<SysDeptVo>> excludeChild(
            @Parameter(description = "部门ID", required = true)
            @PathVariable(value = "deptId", required = false) Long deptId) {
        List<SysDeptVo> depts = deptService.selectDeptList(new SysDeptBo());
        depts.removeIf(d -> d.getDeptId().equals(deptId)
            || StringUtils.splitList(d.getAncestors()).contains(Convert.toStr(deptId)));
        return R.ok(depts);
    }

    /**
     * 根据部门编号获取详细信息
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:query")
    @GetMapping(value = "/{deptId}")
    @Operation(summary = "根据部门编号获取详细信息", description = "根据部门ID获取部门详细信息")
    public R<SysDeptVo> getInfo(
            @Parameter(description = "部门ID", required = true)
            @PathVariable Long deptId) {
        deptService.checkDeptDataScope(deptId);
        return R.ok(deptService.selectDeptById(deptId));
    }

    /**
     * 新增部门
     */
    @SaCheckPermission("system:dept:add")
    @Log(title = "部门管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增部门", description = "新增部门信息")
    public R<Void> add(
            @Parameter(description = "部门信息", required = true)
            @Validated @RequestBody SysDeptBo dept) {
        if (!deptService.checkDeptNameUnique(dept)) {
            return R.fail("新增部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        }
        return toAjax(deptService.insertDept(dept));
    }

    /**
     * 修改部门
     */
    @SaCheckPermission("system:dept:edit")
    @Log(title = "部门管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改部门", description = "修改部门信息")
    public R<Void> edit(
            @Parameter(description = "部门信息", required = true)
            @Validated @RequestBody SysDeptBo dept) {
        Long deptId = dept.getDeptId();
        deptService.checkDeptDataScope(deptId);
        if (!deptService.checkDeptNameUnique(dept)) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(deptId)) {
            return R.fail("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus())) {
            if (deptService.selectNormalChildrenDeptById(deptId) > 0) {
                return R.fail("该部门包含未停用的子部门!");
            } else if (deptService.checkDeptExistUser(deptId)) {
                return R.fail("该部门下存在已分配用户，不能禁用!");
            }
        }
        return toAjax(deptService.updateDept(dept));
    }

    /**
     * 删除部门
     *
     * @param deptId 部门ID
     */
    @SaCheckPermission("system:dept:remove")
    @Log(title = "部门管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deptId}")
    @Operation(summary = "删除部门", description = "根据部门ID删除部门")
    public R<Void> remove(
            @Parameter(description = "部门ID", required = true)
            @PathVariable Long deptId) {
        if (deptService.hasChildByDeptId(deptId)) {
            return R.warn("存在下级部门,不允许删除");
        }
        if (deptService.checkDeptExistUser(deptId)) {
            return R.warn("部门存在用户,不允许删除");
        }
        deptService.checkDeptDataScope(deptId);
        return toAjax(deptService.deleteDeptById(deptId));
    }
}
