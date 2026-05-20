package com.bocoo.system.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.system.domain.bo.SysOperLogBo;
import com.bocoo.system.domain.vo.SysOperLogVo;
import com.bocoo.system.service.SysOperLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 操作日志记录
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/monitor/operlog")
@Tag(name = "操作日志记录", description = "操作日志记录管理接口")
public class SysOperlogController extends BaseController {

    private final SysOperLogService operLogService;

    /**
     * 获取操作日志记录列表
     */
    @SaCheckPermission("monitor:operlog:list")
    @GetMapping("/list")
    @Operation(summary = "获取操作日志记录列表", description = "获取操作日志记录列表")
    public TableDataInfo<SysOperLogVo> list(
            @Parameter(description = "操作日志查询参数")
            SysOperLogBo operLog,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return operLogService.selectPageOperLogList(operLog, pageQuery);
    }

    /**
     * 导出操作日志记录列表
     */
    @Log(title = "操作日志", businessType = BusinessType.EXPORT)
    @SaCheckPermission("monitor:operlog:export")
    @PostMapping("/export")
    @Operation(summary = "导出操作日志记录列表", description = "导出操作日志记录列表")
    public void export(
            @Parameter(description = "操作日志查询参数")
            SysOperLogBo operLog,
            HttpServletResponse response) {
        List<SysOperLogVo> list = operLogService.selectOperLogList(operLog);
        ExcelUtil.exportExcel(list, "操作日志", SysOperLogVo.class, response);
    }

    /**
     * 批量删除操作日志记录
     * @param operIds 日志ids
     */
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @SaCheckPermission("monitor:operlog:remove")
    @DeleteMapping("/{operIds}")
    @Operation(summary = "批量删除操作日志记录", description = "批量删除操作日志记录")
    public R<Void> remove(
            @Parameter(description = "日志IDs", required = true)
            @PathVariable Long[] operIds) {
        return toAjax(operLogService.deleteOperLogByIds(operIds));
    }

    /**
     * 清理操作日志记录
     */
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @SaCheckPermission("monitor:operlog:remove")
    @DeleteMapping("/clean")
    @Operation(summary = "清理操作日志记录", description = "清理操作日志记录")
    public R<Void> clean() {
        operLogService.cleanOperLog();
        return R.ok();
    }
}
