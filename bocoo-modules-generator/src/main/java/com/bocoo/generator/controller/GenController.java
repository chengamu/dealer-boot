package com.bocoo.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.IoUtil;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.generator.domain.GenTable;
import com.bocoo.generator.domain.GenTableColumn;
import com.bocoo.generator.service.IGenTableService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成 操作处理
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/tool/gen")
@Tag(name = "代码生成管理", description = "代码生成操作处理接口")
public class GenController extends BaseController {

    private final IGenTableService genTableService;

    /**
     * 查询代码生成列表
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/list")
    @Operation(summary = "查询代码生成列表", description = "分页查询代码生成列表")
    public TableDataInfo<GenTable> genList(
            @Parameter(description = "代码生成查询参数")
            GenTable genTable,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return genTableService.selectPageGenTableList(genTable, pageQuery);
    }

    /**
     * 修改代码生成业务
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:query")
    @GetMapping(value = "/{tableId}")
    @Operation(summary = "获取代码生成详细信息", description = "根据表ID获取代码生成详细信息")
    public R<Map<String, Object>> getInfo(
            @Parameter(description = "表ID", required = true)
            @PathVariable Long tableId) {
        GenTable table = genTableService.selectGenTableById(tableId);
        List<GenTable> tables = genTableService.selectGenTableAll();
        List<GenTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("info", table);
        map.put("rows", list);
        map.put("tables", tables);
        return R.ok(map);
    }

    /**
     * 查询数据库列表
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping("/db/list")
    @Operation(summary = "查询数据库列表", description = "分页查询数据库表列表")
    public TableDataInfo<GenTable> dataList(
            @Parameter(description = "数据库表查询参数")
            GenTable genTable,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return genTableService.selectPageDbTableList(genTable, pageQuery);
    }

    /**
     * 查询数据表字段列表
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:list")
    @GetMapping(value = "/column/{tableId}")
    @Operation(summary = "查询数据表字段列表", description = "根据表ID查询数据表字段列表")
    public TableDataInfo<GenTableColumn> columnList(
            @Parameter(description = "表ID", required = true)
            @PathVariable("tableId") Long tableId) {
        TableDataInfo<GenTableColumn> dataInfo = new TableDataInfo<>();
        List<GenTableColumn> list = genTableService.selectGenTableColumnListByTableId(tableId);
        dataInfo.setRows(list);
        dataInfo.setTotal(list.size());
        return dataInfo;
    }

    /**
     * 导入表结构（保存）
     *
     * @param tables 表名串
     */
    @SaCheckPermission("tool:gen:import")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PostMapping("/importTable")
    @Operation(summary = "导入表结构", description = "导入数据库表结构到代码生成器")
    public R<Void> importTableSave(
            @Parameter(description = "表名串", required = true)
            String tables) {
        String[] tableNames = Convert.toStrArray(tables);
        // 查询表信息
        List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames);
        genTableService.importGenTable(tableList);
        return R.ok();
    }

    /**
     * 修改保存代码生成业务
     */
    @SaCheckPermission("tool:gen:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改代码生成业务", description = "修改保存代码生成业务配置")
    public R<Void> editSave(
            @Parameter(description = "代码生成信息", required = true)
            @Validated @RequestBody GenTable genTable) {
        genTableService.validateEdit(genTable);
        genTableService.updateGenTable(genTable);
        return R.ok();
    }

    /**
     * 删除代码生成
     *
     * @param tableIds 表ID串
     */
    @SaCheckPermission("tool:gen:remove")
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    @DeleteMapping("/{tableIds}")
    @Operation(summary = "删除代码生成", description = "批量删除代码生成配置")
    public R<Void> remove(
            @Parameter(description = "表ID数组", required = true)
            @PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return R.ok();
    }

    /**
     * 预览代码
     *
     * @param tableId 表ID
     */
    @SaCheckPermission("tool:gen:preview")
    @GetMapping("/preview/{tableId}")
    @Operation(summary = "预览代码", description = "根据表ID预览生成的代码")
    public R<Map<String, String>> preview(
            @Parameter(description = "表ID", required = true)
            @PathVariable("tableId") Long tableId) throws IOException {
        Map<String, String> dataMap = genTableService.previewCode(tableId);
        return R.ok(dataMap);
    }

    /**
     * 生成代码（下载方式）
     *
     * @param tableName 表名
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/download/{tableName}")
    @Operation(summary = "生成代码（下载方式）", description = "根据表名生成代码并以ZIP文件下载")
    public void download(
            HttpServletResponse response,
            @Parameter(description = "表名", required = true)
            @PathVariable("tableName") String tableName) throws IOException {
        byte[] data = genTableService.downloadCode(tableName);
        genCode(response, data);
    }

    /**
     * 生成代码（自定义路径）
     *
     * @param tableName 表名
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/genCode/{tableName}")
    @Operation(summary = "生成代码（自定义路径）", description = "根据表名生成代码到自定义路径")
    public R<Void> genCode(
            @Parameter(description = "表名", required = true)
            @PathVariable("tableName") String tableName) {
        genTableService.generatorCode(tableName);
        return R.ok();
    }

    /**
     * 同步数据库
     *
     * @param tableName 表名
     */
    @SaCheckPermission("tool:gen:edit")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @GetMapping("/synchDb/{tableName}")
    @Operation(summary = "同步数据库", description = "同步数据库表结构到代码生成器")
    public R<Void> synchDb(
            @Parameter(description = "表名", required = true)
            @PathVariable("tableName") String tableName) {
        genTableService.synchDb(tableName);
        return R.ok();
    }

    /**
     * 批量生成代码
     *
     * @param tables 表名串
     */
    @SaCheckPermission("tool:gen:code")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @GetMapping("/batchGenCode")
    @Operation(summary = "批量生成代码", description = "根据表名串批量生成代码并下载")
    public void batchGenCode(
            HttpServletResponse response,
            @Parameter(description = "表名串", required = true)
            String tables) throws IOException {
        String[] tableNames = Convert.toStrArray(tables);
        byte[] data = genTableService.downloadCode(tableNames);
        genCode(response, data);
    }

    /**
     * 生成zip文件
     */
    private void genCode(HttpServletResponse response, byte[] data) throws IOException {
        response.reset();
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment; filename=\"bocoo.zip\"");
        response.addHeader("Content-Length", "" + data.length);
        response.setContentType("application/octet-stream; charset=UTF-8");
        IoUtil.write(response.getOutputStream(), false, data);
    }
}
