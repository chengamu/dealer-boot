package com.bocoo.system.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.core.enums.UserStatus;
import com.bocoo.system.domain.entity.SysDept;
import com.bocoo.system.domain.entity.SysMenu;
import com.bocoo.system.domain.entity.SysRole;
import com.bocoo.system.domain.entity.SysRoleMenu;
import com.bocoo.system.mapper.SysDeptMapper;
import com.bocoo.system.mapper.SysMenuMapper;
import com.bocoo.system.mapper.SysRoleMapper;
import com.bocoo.system.mapper.SysRoleMenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MerchantAccountDefaultsService {

    public static final String DEALER_DEPT_NAME = "Dealer";
    public static final String STORE_DEPT_NAME = "Store";
    public static final String DEALER_ROLE_KEY = "merchant_admin";
    public static final String STORE_ROLE_KEY = "merchant_store";
    public static final String EMPLOYEE_ROLE_KEY = "merchant_employee";

    private final SysDeptMapper deptMapper;
    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    @Transactional(rollbackFor = Exception.class)
    public MerchantDefaults ensureDefaults() {
        SysDept dealerDept = ensureDept(DEALER_DEPT_NAME, 1);
        SysDept storeDept = ensureDept(STORE_DEPT_NAME, 2);
        SysRole dealerRole = ensureRole("Merchant Admin", DEALER_ROLE_KEY, 1);
        SysRole storeRole = ensureRole("Merchant Store", STORE_ROLE_KEY, 2);
        SysRole employeeRole = ensureRole("Merchant Employee", EMPLOYEE_ROLE_KEY, 3);
        List<Long> dealerMenuIds = ensureMerchantMenus(true);
        List<Long> storeMenuIds = ensureMerchantMenus(false);
        List<Long> employeeMenuIds = ensureMerchantMenus(false);
        ensureRoleMenus(dealerRole.getRoleId(), dealerMenuIds);
        ensureRoleMenus(storeRole.getRoleId(), storeMenuIds);
        ensureRoleMenus(employeeRole.getRoleId(), employeeMenuIds);
        return new MerchantDefaults(dealerDept, storeDept, dealerRole, storeRole, employeeRole);
    }

    public SysDept ensureDealerDept() {
        return ensureDept(DEALER_DEPT_NAME, 1);
    }

    public SysDept ensureStoreDept() {
        return ensureDept(STORE_DEPT_NAME, 2);
    }

    public SysRole ensureDealerRole() {
        return ensureRole("Merchant Admin", DEALER_ROLE_KEY, 1);
    }

    public SysRole ensureStoreRole() {
        return ensureRole("Merchant Store", STORE_ROLE_KEY, 2);
    }

    public SysRole ensureEmployeeRole() {
        return ensureRole("Merchant Employee", EMPLOYEE_ROLE_KEY, 3);
    }

    private SysDept ensureDept(String deptName, Integer orderNum) {
        SysDept dept = deptMapper.selectOne(new LambdaQueryWrapper<SysDept>()
            .eq(SysDept::getDeptName, deptName), false);
        if (dept != null) {
            return dept;
        }
        dept = new SysDept();
        dept.setParentId(0L);
        dept.setAncestors("0");
        dept.setDeptName(deptName);
        dept.setOrderNum(orderNum);
        dept.setStatus(UserConstants.DEPT_NORMAL);
        dept.setDelFlag(UserConstants.NOT_DELETED);
        deptMapper.insert(dept);
        return dept;
    }

    private SysRole ensureRole(String roleName, String roleKey, Integer roleSort) {
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
            .eq(SysRole::getRoleKey, roleKey), false);
        if (role != null) {
            return role;
        }
        role = new SysRole();
        role.setRoleName(roleName);
        role.setRoleKey(roleKey);
        role.setRoleSort(roleSort);
        role.setDataScope("1");
        role.setMenuCheckStrictly(true);
        role.setDeptCheckStrictly(true);
        role.setStatus(UserStatus.OK.getCode());
        role.setDelFlag(UserConstants.NOT_DELETED);
        roleMapper.insert(role);
        return role;
    }

    private List<Long> ensureMerchantMenus(boolean includeUserManagement) {
        SysMenu parent = selectOrCreateMenu(null, "Merchant", "menu.merchant", "merchant", UserConstants.TYPE_DIR, null, 10);
        SysMenu profile = selectOrCreateMenu(parent.getMenuId(), "Merchant Profile", "menu.merchant.profile",
            "profile", UserConstants.TYPE_MENU, "merchant:profile:query", 1);
        SysMenu profileQuery = selectOrCreateMenu(profile.getMenuId(), "Merchant Profile Query", "menu.merchant.profile.query",
            "", UserConstants.TYPE_BUTTON, "merchant:profile:query", 1);
        SysMenu profileEdit = selectOrCreateMenu(profile.getMenuId(), "Merchant Profile Edit", "menu.merchant.profile.edit",
            "", UserConstants.TYPE_BUTTON, "merchant:profile:edit", 2);
        SysMenu customers = selectOrCreateMenu(parent.getMenuId(), "Customers", "menu.customer.profile",
            "customers", UserConstants.TYPE_MENU, "customer:profile:list", 3);
        SysMenu customerQuery = selectOrCreateMenu(customers.getMenuId(), "Customer Query", "menu.customer.profile.query",
            "", UserConstants.TYPE_BUTTON, "customer:profile:query", 1);
        SysMenu customerAdd = selectOrCreateMenu(customers.getMenuId(), "Customer Add", "menu.customer.profile.add",
            "", UserConstants.TYPE_BUTTON, "customer:profile:add", 2);
        SysMenu customerEdit = selectOrCreateMenu(customers.getMenuId(), "Customer Edit", "menu.customer.profile.edit",
            "", UserConstants.TYPE_BUTTON, "customer:profile:edit", 3);
        SysMenu customerRemove = selectOrCreateMenu(customers.getMenuId(), "Customer Delete", "menu.customer.profile.remove",
            "", UserConstants.TYPE_BUTTON, "customer:profile:remove", 4);

        List<Long> menuIds = new ArrayList<>(List.of(parent.getMenuId(), profile.getMenuId(), profileQuery.getMenuId(),
            customers.getMenuId(), customerQuery.getMenuId(), customerAdd.getMenuId(), customerEdit.getMenuId(), customerRemove.getMenuId()));
        if (!includeUserManagement) {
            return menuIds;
        }
        menuIds.add(profileEdit.getMenuId());
        SysMenu users = selectOrCreateMenu(parent.getMenuId(), "Merchant Users", "menu.merchant.users",
            "users", UserConstants.TYPE_MENU, "merchant:user:list", 2);
        SysMenu query = selectOrCreateMenu(users.getMenuId(), "Merchant User Query", "menu.merchant.users.query",
            "", UserConstants.TYPE_BUTTON, "merchant:user:query", 1);
        SysMenu add = selectOrCreateMenu(users.getMenuId(), "Merchant User Add", "menu.merchant.users.add",
            "", UserConstants.TYPE_BUTTON, "merchant:user:add", 2);
        SysMenu edit = selectOrCreateMenu(users.getMenuId(), "Merchant User Edit", "menu.merchant.users.edit",
            "", UserConstants.TYPE_BUTTON, "merchant:user:edit", 3);
        SysMenu remove = selectOrCreateMenu(users.getMenuId(), "Merchant User Delete", "menu.merchant.users.remove",
            "", UserConstants.TYPE_BUTTON, "merchant:user:remove", 4);
        SysMenu resetPwd = selectOrCreateMenu(users.getMenuId(), "Merchant User Reset Password", "menu.merchant.users.resetPwd",
            "", UserConstants.TYPE_BUTTON, "merchant:user:resetPwd", 5);
        menuIds.addAll(List.of(users.getMenuId(), query.getMenuId(), add.getMenuId(), edit.getMenuId(), remove.getMenuId(), resetPwd.getMenuId()));
        return menuIds;
    }

    private void ensureRoleMenus(Long roleId, List<Long> menuIds) {
        if (ObjectUtil.isNull(roleId) || menuIds.isEmpty()) {
            return;
        }
        List<SysRoleMenu> roleMenus = new ArrayList<>();
        for (Long menuId : menuIds) {
            boolean exists = roleMenuMapper.exists(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .eq(SysRoleMenu::getMenuId, menuId));
            if (exists) {
                continue;
            }
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenus.add(roleMenu);
        }
        if (!roleMenus.isEmpty()) {
            roleMenuMapper.insertBatch(roleMenus);
        }
    }

    private SysMenu selectOrCreateMenu(Long parentId, String menuName, String i18nKey, String path,
                                      String menuType, String perms, Integer orderNum) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getMenuType, menuType)
            .eq(SysMenu::getMenuName, menuName)
            .eq(parentId != null, SysMenu::getParentId, parentId)
            .eq(ObjectUtil.isNotNull(perms), SysMenu::getPerms, perms);
        SysMenu menu = menuMapper.selectOne(wrapper, false);
        if (menu != null) {
            return menu;
        }
        menu = new SysMenu();
        menu.setMenuName(menuName);
        menu.setI18nKey(i18nKey);
        menu.setParentId(parentId == null ? 0L : parentId);
        menu.setOrderNum(orderNum);
        menu.setPath(path);
        if (UserConstants.TYPE_DIR.equals(menuType)) {
            menu.setComponent(UserConstants.LAYOUT);
        } else if (UserConstants.TYPE_MENU.equals(menuType)) {
            menu.setComponent("users".equals(path) ? "merchant/User" : "customers".equals(path) ? "customer/profile" : "merchant/Profile");
        }
        menu.setIsFrame(UserConstants.NO_FRAME);
        menu.setIsCache("0");
        menu.setMenuType(menuType);
        menu.setVisible(UserConstants.TYPE_BUTTON.equals(menuType) ? "0" : "1");
        menu.setStatus(UserConstants.MENU_NORMAL);
        menu.setPerms(perms);
        menu.setIcon("users".equals(path) ? "user" : UserConstants.TYPE_DIR.equals(menuType) ? "shop" : "form");
        menuMapper.insert(menu);
        return menu;
    }

    public record MerchantDefaults(SysDept dealerDept, SysDept storeDept, SysRole dealerRole, SysRole storeRole, SysRole employeeRole) {
    }
}
