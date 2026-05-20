package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.core.constant.UserConstants;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.system.domain.bo.SysPostBo;
import com.bocoo.system.domain.vo.SysPostVo;
import com.bocoo.system.service.SysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 岗位信息操作处理
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/post")
@Tag(name = "岗位管理", description = "岗位信息操作处理接口")
public class SysPostController extends BaseController {

    private final SysPostService postService;

    /**
     * 获取岗位列表
     */
    @SaCheckPermission("system:post:list")
    @GetMapping("/list")
    @Operation(summary = "获取岗位列表", description = "分页获取岗位列表")
    public TableDataInfo<SysPostVo> list(
            @Parameter(description = "岗位查询参数")
            SysPostBo post,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return postService.selectPagePostList(post, pageQuery);
    }

    /**
     * 导出岗位列表
     */
    @Log(title = "岗位管理", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:post:export")
    @PostMapping("/export")
    @Operation(summary = "导出岗位列表", description = "导出岗位列表到Excel")
    public void export(
            @Parameter(description = "岗位查询参数")
            SysPostBo post,
            HttpServletResponse response) {
        List<SysPostVo> list = postService.selectPostList(post);
        ExcelUtil.exportExcel(list, "岗位数据", SysPostVo.class, response);
    }

    /**
     * 根据岗位编号获取详细信息
     *
     * @param postId 岗位ID
     */
    @SaCheckPermission("system:post:query")
    @GetMapping(value = "/{postId}")
    @Operation(summary = "根据岗位编号获取详细信息", description = "根据岗位ID获取岗位详细信息")
    public R<SysPostVo> getInfo(
            @Parameter(description = "岗位ID", required = true)
            @PathVariable Long postId) {
        return R.ok(postService.selectPostById(postId));
    }

    /**
     * 新增岗位
     */
    @SaCheckPermission("system:post:add")
    @Log(title = "岗位管理", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增岗位", description = "新增岗位信息")
    public R<Void> add(
            @Parameter(description = "岗位信息", required = true)
            @Validated @RequestBody SysPostBo post) {
        if (!postService.checkPostNameUnique(post)) {
            return R.fail("新增岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        } else if (!postService.checkPostCodeUnique(post)) {
            return R.fail("新增岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        }
        return toAjax(postService.insertPost(post));
    }

    /**
     * 修改岗位
     */
    @SaCheckPermission("system:post:edit")
    @Log(title = "岗位管理", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改岗位", description = "修改岗位信息")
    public R<Void> edit(
            @Parameter(description = "岗位信息", required = true)
            @Validated @RequestBody SysPostBo post) {
        if (!postService.checkPostNameUnique(post)) {
            return R.fail("修改岗位'" + post.getPostName() + "'失败，岗位名称已存在");
        } else if (!postService.checkPostCodeUnique(post)) {
            return R.fail("修改岗位'" + post.getPostName() + "'失败，岗位编码已存在");
        } else if (UserConstants.POST_DISABLE.equals(post.getStatus())
            && postService.countUserPostById(post.getPostId()) > 0) {
            return R.fail("该岗位下存在已分配用户，不能禁用!");
        }
        return toAjax(postService.updatePost(post));
    }

    /**
     * 删除岗位
     *
     * @param postIds 岗位ID串
     */
    @SaCheckPermission("system:post:remove")
    @Log(title = "岗位管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{postIds}")
    @Operation(summary = "删除岗位", description = "批量删除岗位")
    public R<Void> remove(
            @Parameter(description = "岗位ID数组", required = true)
            @PathVariable Long[] postIds) {
        return toAjax(postService.deletePostByIds(postIds));
    }

    /**
     * 获取岗位选择框列表
     */
    @GetMapping("/optionselect")
    @Operation(summary = "获取岗位选择框列表", description = "获取正常状态的岗位选择框列表")
    public R<List<SysPostVo>> optionselect() {
        SysPostBo post = new SysPostBo();
        post.setStatus(UserConstants.POST_NORMAL);
        List<SysPostVo> posts = postService.selectPostList(post);
        return R.ok(posts);
    }
}
