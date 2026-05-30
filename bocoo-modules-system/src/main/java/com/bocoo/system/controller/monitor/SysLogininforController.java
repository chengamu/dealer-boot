package com.bocoo.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.core.constant.CacheConstants;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.redis.utils.RedisUtils;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.system.domain.bo.SysLogininforBo;
import com.bocoo.system.domain.entity.SysLogininfor;
import com.bocoo.system.domain.vo.SysLogininforVo;
import com.bocoo.system.service.SysLogininforService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统访问记录
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/logininfor")
@Tag(name = "系统访问记录", description = "系统访问记录管理接口")
public class SysLogininforController extends BaseController {

    private final SysLogininforService sysLogininforService;

    /**
     * 获取系统访问记录列表
     */
    @SaCheckPermission("monitor:logininfor:list")
    @GetMapping("/list")
    @Operation(summary = "获取系统访问记录列表", description = "获取系统访问记录列表")
    public TableDataInfo<SysLogininforVo> list(
            @Parameter(description = "访问记录查询参数")
            SysLogininforBo logininfor,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return sysLogininforService.selectPageLogininforList(logininfor, pageQuery);
    }

    /**
     * 导出系统访问记录列表
     */
    @Log(title = "登录日志", businessType = BusinessType.EXPORT)
    @SaCheckPermission("monitor:logininfor:export")
    @PostMapping("/export")
    @Operation(summary = "导出系统访问记录列表", description = "导出系统访问记录列表")
    public void export(
            @Parameter(description = "访问记录查询参数")
            SysLogininforBo logininfor,
            HttpServletResponse response) {
        List<SysLogininfor> list = sysLogininforService.selectLogininforList(logininfor);
        ExcelUtil.exportExcel(list, "登录日志", SysLogininfor.class, response);
    }

    /**
     * 批量删除登录日志
     * @param infoIds 日志ids
     */
    @SaCheckPermission("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.SENSITIVE_OPERATION)
    @DeleteMapping("/{infoIds}")
    @Operation(summary = "批量删除登录日志", description = "批量删除登录日志")
    public R<Void> remove(
            @Parameter(description = "日志IDs", required = true)
            @PathVariable Long[] infoIds) {
        return toAjax(sysLogininforService.deleteLogininforByIds(infoIds));
    }

    /**
     * 清理系统访问记录
     */
    @SaCheckPermission("monitor:logininfor:remove")
    @Log(title = "登录日志", businessType = BusinessType.SENSITIVE_OPERATION)
    @DeleteMapping("/clean")
    @Operation(summary = "清理系统访问记录", description = "清理系统访问记录")
    public R<Void> clean() {
        sysLogininforService.cleanLogininfor();
        return R.ok();
    }

    @SaCheckPermission("monitor:logininfor:unlock")
    @Log(title = "账户解锁", businessType = BusinessType.SENSITIVE_OPERATION)
    @GetMapping("/unlock/{userName}")
    @Operation(summary = "账户解锁", description = "根据用户名解锁账户")
    public R<Void> unlock(
            @Parameter(description = "用户名", required = true)
            @PathVariable("userName") String userName) {
        RedisUtils.deleteKeys(CacheConstants.PWD_ERR_CNT_KEY + userName + "*");
        return R.ok();
    }

}
