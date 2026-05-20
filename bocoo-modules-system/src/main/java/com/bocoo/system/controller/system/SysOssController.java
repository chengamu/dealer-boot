package com.bocoo.system.controller.system;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.file.MimeTypeUtils;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.validate.QueryGroup;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.system.domain.bo.SysOssBo;
import com.bocoo.system.domain.vo.SysOssVo;
import com.bocoo.system.service.SysOssService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 文件上传 控制层
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/oss")
@Tag(name = "对象存储管理", description = "OSS对象存储管理接口")
public class SysOssController extends BaseController {

    private final SysOssService sysSssService;

    /**
     * 查询OSS对象存储列表
     */
    @SaCheckPermission("system:oss:list")
    @GetMapping("/list")
    @Operation(summary = "查询OSS对象存储列表", description = "分页查询OSS对象存储列表")
    public TableDataInfo<SysOssVo> list(
            @Parameter(description = "OSS对象存储查询参数")
            @Validated(QueryGroup.class) SysOssBo bo,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return sysSssService.queryPageList(bo, pageQuery);
    }

    /**
     * 查询OSS对象基于id串
     *
     * @param ossIds OSS对象ID串
     */
    @SaCheckPermission("system:oss:list")
    @GetMapping("/listByIds/{ossIds}")
    @Operation(summary = "查询OSS对象基于ID串", description = "根据ID串查询OSS对象列表")
    public R<List<SysOssVo>> listByIds(
            @Parameter(description = "OSS对象ID数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] ossIds) {
        List<SysOssVo> list = sysSssService.listByIds(Arrays.asList(ossIds));
        return R.ok(list);
    }

    /**
     * 上传OSS对象存储
     *
     * @param file 文件
     */
    @SaCheckPermission("system:oss:upload")
    @Log(title = "OSS对象存储", businessType = BusinessType.INSERT)
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传OSS对象存储", description = "上传文件到OSS对象存储")
    public R<Map<String, String>> upload(
            @Parameter(description = "上传文件", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary")))
            @RequestPart("file") MultipartFile file) {
        if (ObjectUtil.isNull(file)) {
            throw new ServiceException("上传文件不能为空");
        }
        SysOssVo oss = sysSssService.upload(file);
        return R.ok(Map.of(
            "url", oss.getUrl(),
            "fileName", oss.getOriginalName(),
            "ossId", oss.getOssId().toString()
        ));
    }

    /**
     * 头像图片上传接口
     *
     * @param avatarfile 用户上传的头像文件，通过multipart/form-data格式传输
     * @return 返回包含图片URL的响应结果，成功时返回图片访问地址，失败时返回错误信息
     */
    @SaCheckPermission("system:oss:upload")
    @Log(title = "图片上传", businessType = BusinessType.UPDATE)
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public R<Map<String, Object>> avatar(@RequestPart("avatarfile") MultipartFile avatarfile) {
        // 检查上传文件是否为空
        if (!avatarfile.isEmpty()) {
            // 获取文件扩展名并验证是否为支持的图片格式
            String extension = FileUtil.extName(avatarfile.getOriginalFilename());
            if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION)) {
                return R.fail("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
            }
            // 上传文件到OSS并返回访问URL
            SysOssVo oss = sysSssService.upload(avatarfile);
            return R.ok(Map.of("imgUrl", oss.getUrl()));

        }
        return R.fail("上传图片异常，请联系管理员");
    }

    /**
     * 下载OSS对象
     *
     * @param ossId OSS对象ID
     */
    @SaCheckPermission("system:oss:download")
    @GetMapping("/download/{ossId}")
    @Operation(summary = "下载OSS对象", description = "根据OSS对象ID下载文件")
    public void download(
            @Parameter(description = "OSS对象ID", required = true)
            @PathVariable Long ossId,
            HttpServletResponse response) throws IOException {
        sysSssService.download(ossId,response);
    }

    /**
     * 删除OSS对象存储
     *
     * @param ossIds OSS对象ID串
     */
    @SaCheckPermission("system:oss:remove")
    @Log(title = "OSS对象存储", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ossIds}")
    @Operation(summary = "删除OSS对象存储", description = "批量删除OSS对象存储")
    public R<Void> remove(
            @Parameter(description = "OSS对象ID数组", required = true)
            @NotEmpty(message = "主键不能为空")
            @PathVariable Long[] ossIds) {
        return toAjax(sysSssService.deleteWithValidByIds(List.of(ossIds), true));
    }

}
