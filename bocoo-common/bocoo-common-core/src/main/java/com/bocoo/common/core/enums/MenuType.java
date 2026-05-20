package com.bocoo.common.core.enums;

public enum MenuType {
    A("功能"), M("目录"), V("菜单"), G("分组");

    public String desc;

    MenuType(String desc) {
        this.desc = desc;
    }
}
