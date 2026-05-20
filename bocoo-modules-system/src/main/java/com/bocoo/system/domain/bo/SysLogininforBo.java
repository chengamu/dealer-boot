package com.bocoo.system.domain.bo;

import com.bocoo.system.domain.entity.SysLogininfor;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统访问记录业务对象 sys_logininfor
 *
 * @author CMX
 */

@Data
@AutoMapper(target = SysLogininfor.class, reverseConvertGenerate = false)
@Schema(description = "系统访问记录业务对象")
public class SysLogininforBo {

    /**
     * 访问ID
     */
    @Schema(description = "访问ID")
    private Long infoId;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号")
    private String userName;

    /**
     * 设备类型
     */
    @Schema(description = "设备类型")
    private String deviceType;

    /**
     * 登录IP地址
     */
    @Schema(description = "登录IP地址")
    private String ipaddr;

    /**
     * 登录地点
     */
    @Schema(description = "登录地点")
    private String loginLocation;

    /**
     * 浏览器类型
     */
    @Schema(description = "浏览器类型")
    private String browser;

    /**
     * 操作系统
     */
    @Schema(description = "操作系统")
    private String os;

    /**
     * 登录状态（0失败 1成功）
     */
    @Schema(description = "登录状态（0失败 1成功）")
    private String status;

    /**
     * 提示消息
     */
    @Schema(description = "提示消息")
    private String msg;

    /**
     * 访问时间
     */
    @Schema(description = "访问时间")
    private LocalDateTime loginTime;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private Map<String, Object> params = new HashMap<>();


}
