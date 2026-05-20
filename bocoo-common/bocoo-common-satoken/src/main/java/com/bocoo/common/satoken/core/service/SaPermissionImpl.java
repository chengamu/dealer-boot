package com.bocoo.common.satoken.core.service;

import cn.dev33.satoken.stp.StpInterface;
import com.bocoo.common.core.domain.bo.LoginUser;
import com.bocoo.common.core.enums.UserType;
import com.bocoo.common.satoken.utils.LoginHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * sa-token 权限管理实现类
 *
 * @author Lion Li
 */
public class SaPermissionImpl implements StpInterface {



    /**
     * 获取用户权限列表
     * @param loginId 登录用户ID
     * @param loginType 登录类型
     * @return 用户权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        UserType userType = null;
        if (loginUser != null) {
            userType = UserType.getUserType(loginUser.getUserType());
        }
        // 系统用户直接返回菜单权限
        if (userType == UserType.SYS_USER) {
            return new ArrayList<>(loginUser.getMenuPermission());
        } else if (userType == UserType.APP_USER) {
            // 其他端 自行根据业务编写
        }
        return new ArrayList<>();
    }


    /**
     * 获取角色权限列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        LoginUser loginUser = LoginHelper.getLoginUser();
        UserType userType = null;
        if (loginUser != null) {
            userType = UserType.getUserType(loginUser.getUserType());
        }
        if (userType == UserType.SYS_USER) {
            return new ArrayList<>(loginUser.getRolePermission());
        } else if (userType == UserType.APP_USER) {
            // 其他端 自行根据业务编写
        }
        return new ArrayList<>();
    }
}
