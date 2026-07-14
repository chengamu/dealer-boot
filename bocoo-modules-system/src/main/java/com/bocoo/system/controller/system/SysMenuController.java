package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.lang.tree.Tree;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.system.domain.bo.SysMenuBo;
import com.bocoo.system.domain.vo.SysMenuVo;
import com.bocoo.system.service.SysMenuCommandService;
import com.bocoo.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单信息
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/menu")
@Tag(name = "菜单管理", description = "菜单信息管理接口")
public class SysMenuController extends BaseController {

    private final SysMenuService menuService;
    private final SysMenuCommandService menuCommandService;

    /**
     * 获取菜单列表
     */
    @SaCheckPermission("system:menu:list")
    @GetMapping("/list")
    @Operation(summary = "获取菜单列表", description = "获取菜单列表")
    public R<List<SysMenuVo>> list(
            @Parameter(description = "菜单查询参数")
            SysMenuBo menu) {
        List<SysMenuVo> menus = menuService.selectMenuList(menu, LoginHelper.getUserId());
        return R.ok(menus);
    }

    /**
     * 根据菜单编号获取详细信息
     *
     * @param menuId 菜单ID
     */
    @SaCheckPermission("system:menu:query")
    @GetMapping(value = "/{menuId}")
    @Operation(summary = "根据菜单编号获取详细信息", description = "根据菜单ID获取菜单详细信息")
    public R<SysMenuVo> getInfo(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable Long menuId) {
        return R.ok(menuService.selectMenuById(menuId));
    }

    /**
     * 获取菜单下拉树列表
     */
    @GetMapping("/treeselect")
    @Operation(summary = "获取菜单下拉树列表", description = "获取菜单下拉树列表")
    public R<List<Tree<Long>>> treeselect(
            @Parameter(description = "菜单查询参数")
            SysMenuBo menu) {
        List<SysMenuVo> menus = menuService.selectMenuList(menu, LoginHelper.getUserId());
        return R.ok(menuService.buildMenuTreeSelect(menus));
    }

    /**
     * 加载对应角色菜单列表树
     *
     * @param roleId 角色ID
     */
    @GetMapping(value = "/roleMenuTreeselect/{roleId}")
    @Operation(summary = "加载对应角色菜单列表树", description = "根据角色ID加载菜单列表树")
    public R<Map<String, Object>> roleMenuTreeselect(
            @Parameter(description = "角色ID", required = true)
            @PathVariable("roleId") Long roleId) {
        List<SysMenuVo> menus = menuService.selectMenuList(LoginHelper.getUserId());
        return R.ok(Map.of(
            "checkedKeys", menuService.selectMenuListByRoleId(roleId),
            "menus", menuService.buildMenuTreeSelect(menus)
        ));
    }

    /**
     * 新增菜单
     */
    @SaCheckPermission("system:menu:add")
    @Log(title = "菜单管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增菜单", description = "新增菜单信息")
    public R<Void> add(
            @Parameter(description = "菜单信息", required = true)
            @Validated @RequestBody SysMenuBo menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return R.fail("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return R.fail("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        }
        return toAjax(menuCommandService.insertMenu(menu));
    }

    /**
     * 修改菜单
     */
    @SaCheckPermission("system:menu:edit")
    @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改菜单", description = "修改菜单信息")
    public R<Void> edit(
            @Parameter(description = "菜单信息", required = true)
            @Validated @RequestBody SysMenuBo menu) {
        if (!menuService.checkMenuNameUnique(menu)) {
            return R.fail("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
        } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
            return R.fail("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
        } else if (menu.getMenuId().equals(menu.getParentId())) {
            return R.fail("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
        }
        return toAjax(menuCommandService.updateMenu(menu));
    }

    /**
     * 删除菜单
     *
     * @param menuId 菜单ID
     */
    @SaCheckPermission("system:menu:remove")
    @Log(title = "菜单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{menuId}")
    @Operation(summary = "删除菜单", description = "根据菜单ID删除菜单")
    public R<Void> remove(
            @Parameter(description = "菜单ID", required = true)
            @PathVariable("menuId") Long menuId) {
        if (menuService.hasChildByMenuId(menuId)) {
            return R.warn(MessageUtils.message("menu.children.delete.denied"));
        }
        if (menuService.checkMenuExistRole(menuId)) {
            return R.warn(MessageUtils.message("menu.assigned.delete.denied"));
        }
        return toAjax(menuCommandService.deleteMenuById(menuId));
    }
}
