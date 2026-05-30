package com.bocoo.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.core.constant.CacheConstants;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.domain.vo.UserOnlineVO;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.core.utils.StreamUtils;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.redis.utils.RedisUtils;
import com.bocoo.system.domain.entity.SysUserOnline;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 在线用户监控
 *
 * @author CMX
 */
@RequiredArgsConstructor
@Slf4j
@RestController
@RequestMapping("/monitor/online")
@Tag(name = "在线用户监控", description = "在线用户监控管理接口")
public class SysUserOnlineController extends BaseController {

    /**
     * 获取在线用户监控列表
     *
     * @param ipaddr   IP地址
     * @param userName 用户名
     */
    @SaCheckPermission("monitor:online:list")
    @GetMapping("/list")
    @Operation(summary = "获取在线用户监控列表", description = "获取在线用户监控列表")
    public TableDataInfo<SysUserOnline> list(
            @Parameter(description = "IP地址")
            String ipaddr,
            @Parameter(description = "用户名")
            String userName) {
        // 获取所有未过期的 token
        List<String> keys = StpUtil.searchTokenValue("", 0, CacheConstants.ONLINE_TOKEN_SCAN_LIMIT, false);
        List<UserOnlineVO> userOnlineDTOList = new ArrayList<>();
        for (String key : keys) {
            String token = StringUtils.substringAfterLast(key, ":");
            // 如果已经过期则跳过
            if (StpUtil.stpLogic.getTokenActiveTimeoutByToken(token) < -1) {
                continue;
            }
            userOnlineDTOList.add(RedisUtils.getCacheObject(CacheConstants.ONLINE_TOKEN_KEY + token));
        }
        if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(ipaddr, userOnline.getIpaddr()) &&
                    StringUtils.equals(userName, userOnline.getUserName())
            );
        } else if (StringUtils.isNotEmpty(ipaddr)) {
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(ipaddr, userOnline.getIpaddr())
            );
        } else if (StringUtils.isNotEmpty(userName)) {
            userOnlineDTOList = StreamUtils.filter(userOnlineDTOList, userOnline ->
                StringUtils.equals(userName, userOnline.getUserName())
            );
        }
        Collections.reverse(userOnlineDTOList);
        userOnlineDTOList.removeAll(Collections.singleton(null));
        List<SysUserOnline> userOnlineList = BeanUtil.copyToList(userOnlineDTOList, SysUserOnline.class);
        return TableDataInfo.build(userOnlineList);
    }

    /**
     * 强退用户
     *
     * @param tokenId token值
     */
    @SaCheckPermission("monitor:online:forceLogout")
    @Log(title = "在线用户", businessType = BusinessType.SENSITIVE_OPERATION)
    @DeleteMapping("/{tokenId}")
    @Operation(summary = "强退用户", description = "强制用户下线")
    public R<Void> forceLogout(
            @Parameter(description = "token值", required = true)
            @PathVariable String tokenId) {
        try {
            StpUtil.kickoutByTokenValue(tokenId);
        } catch (NotLoginException ignored) {
            log.debug("Skip force logout because token is not logged in.");
        }
        return R.ok();
    }
}
