package com.bocoo.system.domain.bo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.bocoo.common.core.constant.RegexConstants;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.system.domain.entity.SysMenu;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单权限业务对象 sys_menu
 *
 * @author CMX
 */

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysMenu.class, reverseConvertGenerate = false)
@Schema(description = "菜单权限业务对象")
public class SysMenuBo extends BaseEntity {

    /**
     * 菜单ID
     */
    @Schema(description = "菜单ID")
    private Long menuId;

    /**
     * 父菜单ID
     */
    @Schema(description = "父菜单ID")
    private Long parentId;

    /**
     * 菜单名称
     */
    @Schema(description = "菜单名称")
    @NotBlank(message = "{validation.menu.name.required}")
    @Size(min = 0, max = 50, message = "{validation.menu.name.max}")
    private String menuName;

    /**
     * 国际化消息键
     */
    @Schema(description = "国际化消息键")
    @Size(min = 0, max = 128, message = "{validation.i18n.key.max}")
    private String i18nKey;

    /**
     * 显示顺序
     */
    @Schema(description = "显示顺序")
    @NotNull(message = "{validation.sort.required}")
    private Integer orderNum;

    /**
     * 路由地址
     */
    @Schema(description = "路由地址")
    @Size(min = 0, max = 200, message = "{validation.menu.path.max}")
    private String path;

    /**
     * 组件路径
     */
    @Schema(description = "组件路径")
    @Size(min = 0, max = 200, message = "{validation.menu.component.max}")
    private String component;

    /**
     * 路由参数
     */
    @Schema(description = "路由参数")
    private String queryParam;

    /**
     * 是否为外链（0是 1否）
     */
    @Schema(description = "是否为外链（0是 1否）")
    private String isFrame;

    /**
     * 是否缓存（0缓存 1不缓存）
     */
    @Schema(description = "是否缓存（0缓存 1不缓存）")
    private String isCache;

    /**
     * 菜单类型（M目录 C菜单 F按钮）
     */
    @Schema(description = "菜单类型（M目录 C菜单 F按钮）")
    @NotBlank(message = "{validation.menu.type.required}")
    private String menuType;

    /**
     * 显示状态（0显示 1隐藏）
     */
    @Schema(description = "显示状态（0显示 1隐藏）")
    private String visible;

    /**
     * 菜单状态（0停用 1正常）
     */
    @Schema(description = "菜单状态（0停用 1正常）")
    private String status;

    /**
     * 权限标识
     */
    @Schema(description = "权限标识")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Size(min = 0, max = 100, message = "{validation.menu.permission.max}")
    @Pattern(regexp = RegexConstants.PERMISSION_STRING, message = "{validation.menu.permission.pattern}")
    private String perms;

    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标")
    private String icon;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
