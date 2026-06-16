package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.io.FileUtil;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.core.utils.MessageUtils;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductImportBatchBo;
import com.bocoo.product.domain.bo.ProductImportRowIssueBo;
import com.bocoo.product.domain.vo.ProductImportBatchVo;
import com.bocoo.product.domain.vo.ProductImportRowIssueVo;
import com.bocoo.product.service.ProductImportBatchService;
import com.bocoo.product.service.ProductImportParseService;
import com.bocoo.product.service.ProductImportRowIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 产品能力导入中心接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability/import")
@Tag(name = "产品能力导入中心", description = "产品能力导入中心接口")
public class ProductImportController extends BaseController {

    private final ProductImportBatchService productImportBatchService;
    private final ProductImportParseService productImportParseService;
    private final ProductImportRowIssueService productImportRowIssueService;

    @SaCheckPermission("product:import:list")
    @GetMapping("/batches/list")
    @Operation(summary = "分页查询产品能力导入批次")
    public TableDataInfo<ProductImportBatchVo> listImportBatch(ProductImportBatchBo bo, PageQuery pageQuery) {
        return productImportBatchService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:import:query")
    @GetMapping("/batches/{batchId}")
    @Operation(summary = "获取产品能力导入批次详情")
    public R<ProductImportBatchVo> getImportBatch(@Parameter(description = "导入批次ID", required = true) @PathVariable Long batchId) {
        return R.ok(productImportBatchService.queryById(batchId));
    }

    @SaCheckPermission("product:import:add")
    @Log(title = "解析产品能力导入文件", businessType = BusinessType.IMPORT)
    @PostMapping(value = "/batches/parse-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "解析产品能力XLS/XLSX文件")
    public R<ProductImportBatchVo> parseImportExcel(
        @Parameter(description = "导入文件", content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary")))
        @RequestPart("file") MultipartFile file,
        ProductImportBatchBo bo) throws Exception {
        if (file == null || file.isEmpty()
            || !StringUtils.equalsAnyIgnoreCase(FileUtil.extName(file.getOriginalFilename()), "xls", "xlsx")) {
            return R.fail(MessageUtils.message("upload.invalidFileType", Map.of("types", "xls/xlsx")));
        }
        return R.ok(productImportParseService.parseImportExcel(file, bo));
    }

    @SaCheckPermission("product:import:add")
    @Log(title = "产品能力导入批次", businessType = BusinessType.INSERT)
    @PostMapping("/batches")
    @Operation(summary = "新增产品能力导入批次")
    public R<Void> addImportBatch(@Validated @RequestBody ProductImportBatchBo bo) {
        return toAjax(productImportBatchService.save(bo));
    }

    @SaCheckPermission("product:import:edit")
    @Log(title = "产品能力导入批次", businessType = BusinessType.UPDATE)
    @PutMapping("/batches")
    @Operation(summary = "修改产品能力导入批次")
    public R<Void> editImportBatch(@Validated @RequestBody ProductImportBatchBo bo) {
        return toAjax(productImportBatchService.save(bo));
    }

    @SaCheckPermission("product:import:edit")
    @Log(title = "修改产品能力导入状态", businessType = BusinessType.UPDATE)
    @PutMapping("/batches/change-status/{batchId}/{status}")
    @Operation(summary = "修改产品能力导入状态")
    public R<Void> changeImportBatchStatus(@PathVariable Long batchId, @PathVariable String status) {
        return toAjax(productImportBatchService.updateStatus(batchId, status));
    }

    @SaCheckPermission("product:import:remove")
    @Log(title = "产品能力导入批次", businessType = BusinessType.DELETE)
    @DeleteMapping("/batches/{ids}")
    @Operation(summary = "删除产品能力导入批次")
    public R<Void> removeImportBatch(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productImportBatchService.deleteByIds(ids));
    }

    @SaCheckPermission("product:import:list")
    @GetMapping("/issues/list")
    @Operation(summary = "分页查询产品能力导入行问题")
    public TableDataInfo<ProductImportRowIssueVo> listImportRowIssue(ProductImportRowIssueBo bo, PageQuery pageQuery) {
        return productImportRowIssueService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:import:query")
    @GetMapping("/issues/{issueId}")
    @Operation(summary = "获取产品能力导入行问题详情")
    public R<ProductImportRowIssueVo> getImportRowIssue(@Parameter(description = "导入行问题ID", required = true) @PathVariable Long issueId) {
        return R.ok(productImportRowIssueService.queryById(issueId));
    }

    @SaCheckPermission("product:import:edit")
    @Log(title = "产品能力导入行问题", businessType = BusinessType.INSERT)
    @PostMapping("/issues")
    @Operation(summary = "新增产品能力导入行问题")
    public R<Void> addImportRowIssue(@Validated @RequestBody ProductImportRowIssueBo bo) {
        return toAjax(productImportRowIssueService.save(bo));
    }

    @SaCheckPermission("product:import:edit")
    @Log(title = "产品能力导入行问题", businessType = BusinessType.UPDATE)
    @PutMapping("/issues")
    @Operation(summary = "修改产品能力导入行问题")
    public R<Void> editImportRowIssue(@Validated @RequestBody ProductImportRowIssueBo bo) {
        return toAjax(productImportRowIssueService.save(bo));
    }

    @SaCheckPermission("product:import:remove")
    @Log(title = "产品能力导入行问题", businessType = BusinessType.DELETE)
    @DeleteMapping("/issues/{ids}")
    @Operation(summary = "删除产品能力导入行问题")
    public R<Void> removeImportRowIssue(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productImportRowIssueService.deleteByIds(ids));
    }
}
