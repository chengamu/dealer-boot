package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.system.domain.bo.SysNoticeBo;
import com.bocoo.system.domain.vo.SysNoticeVo;
import com.bocoo.system.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 公告 信息操作处理
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/notice")
@Tag(name = "通知公告管理", description = "通知公告信息操作处理接口")
public class SysNoticeController extends BaseController {

    private final SysNoticeService noticeService;

    /**
     * 获取通知公告列表
     */
    @SaCheckPermission("system:notice:list")
    @GetMapping("/list")
    @Operation(summary = "获取通知公告列表", description = "分页获取通知公告列表")
    public TableDataInfo<SysNoticeVo> list(
            @Parameter(description = "通知公告查询参数")
            SysNoticeBo notice,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return noticeService.selectPageNoticeList(notice, pageQuery);
    }

    /**
     * 根据通知公告编号获取详细信息
     *
     * @param noticeId 公告ID
     */
    @SaCheckPermission("system:notice:query")
    @GetMapping(value = "/{noticeId}")
    @Operation(summary = "根据通知公告编号获取详细信息", description = "根据公告ID获取通知公告详细信息")
    public R<SysNoticeVo> getInfo(
            @Parameter(description = "公告ID", required = true)
            @PathVariable Long noticeId) {
        return R.ok(noticeService.selectNoticeById(noticeId));
    }

    /**
     * 新增通知公告
     */
    @SaCheckPermission("system:notice:add")
    @Log(title = "通知公告", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增通知公告", description = "新增通知公告信息")
    public R<Void> add(
            @Parameter(description = "通知公告信息", required = true)
            @Validated @RequestBody SysNoticeBo notice) {
        return toAjax(noticeService.insertNotice(notice));
    }

    /**
     * 修改通知公告
     */
    @SaCheckPermission("system:notice:edit")
    @Log(title = "通知公告", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改通知公告", description = "修改通知公告信息")
    public R<Void> edit(
            @Parameter(description = "通知公告信息", required = true)
            @Validated @RequestBody SysNoticeBo notice) {
        return toAjax(noticeService.updateNotice(notice));
    }

    /**
     * 删除通知公告
     *
     * @param noticeIds 公告ID串
     */
    @SaCheckPermission("system:notice:remove")
    @Log(title = "通知公告", businessType = BusinessType.DELETE)
    @DeleteMapping("/{noticeIds}")
    @Operation(summary = "删除通知公告", description = "批量删除通知公告")
    public R<Void> remove(
            @Parameter(description = "公告ID数组", required = true)
            @PathVariable Long[] noticeIds) {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }
}
