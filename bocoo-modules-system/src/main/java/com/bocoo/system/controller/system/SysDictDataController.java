package com.bocoo.system.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.ratelimiter.annotation.RateLimiter;
import com.bocoo.common.ratelimiter.enums.LimitType;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.excel.utils.ExcelUtil;
import com.bocoo.system.domain.bo.SysDictDataBo;
import com.bocoo.system.domain.vo.SysDictDataVo;
import com.bocoo.system.service.SysDictDataService;
import com.bocoo.system.service.SysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典信息
 *
 * @author CMX
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dict/data")
@Tag(name = "数据字典管理", description = "数据字典信息管理接口")
public class SysDictDataController extends BaseController {

    private static final String PUBLIC_COUNTRY_DICT = "sys_country";

    private final SysDictDataService dictDataService;
    private final SysDictTypeService dictTypeService;

    /**
     * 查询字典数据列表
     */
    @SaCheckPermission("system:dict:list")
    @GetMapping("/list")
    @Operation(summary = "查询字典数据列表", description = "分页查询字典数据列表")
    public TableDataInfo<SysDictDataVo> list(
            @Parameter(description = "字典数据查询参数")
            SysDictDataBo dictData,
            @Parameter(description = "分页参数")
            PageQuery pageQuery) {
        return dictDataService.selectPageDictDataList(dictData, pageQuery);
    }

    /**
     * 导出字典数据列表
     */
    @Log(title = "字典数据", businessType = BusinessType.EXPORT)
    @SaCheckPermission("system:dict:export")
    @PostMapping("/export")
    @Operation(summary = "导出字典数据列表", description = "导出字典数据列表到Excel")
    public void export(
            @Parameter(description = "字典数据查询参数")
            SysDictDataBo dictData,
            HttpServletResponse response) {
        List<SysDictDataVo> list = dictDataService.selectDictDataList(dictData);
        ExcelUtil.exportExcel(list, "字典数据", SysDictDataVo.class, response);
    }

    /**
     * 查询字典数据详细
     *
     * @param dictCode 字典code
     */
    @SaCheckPermission("system:dict:query")
    @GetMapping(value = "/{dictCode}")
    @Operation(summary = "查询字典数据详细", description = "根据字典代码查询字典数据详细信息")
    public R<SysDictDataVo> getInfo(
            @Parameter(description = "字典代码", required = true)
            @PathVariable Long dictCode) {
        return R.ok(dictDataService.selectDictDataById(dictCode));
    }

    /**
     * 根据字典类型查询字典数据信息
     *
     * @param dictType 字典类型
     */
    @SaIgnore
    @RateLimiter(count = 180, time = 60, limitType = LimitType.IP)
    @GetMapping(value = "/type/{dictType}")
    @Operation(summary = "根据字典类型查询字典数据信息", description = "根据字典类型查询字典数据信息")
    public R<List<SysDictDataVo>> dictType(
            @Parameter(description = "字典类型", required = true)
            @PathVariable String dictType) {
        if (!PUBLIC_COUNTRY_DICT.equals(dictType)) {
            StpUtil.checkLogin();
        }
        List<SysDictDataVo> data = dictTypeService.selectTranslatedDictDataByType(dictType);
        if (ObjectUtil.isNull(data)) {
            data = new ArrayList<>();
        }
        return R.ok(data);
    }

    /**
     * 新增字典类型
     */
    @SaCheckPermission("system:dict:add")
    @Log(title = "字典数据", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增字典数据", description = "新增字典数据信息")
    public R<Void> add(
            @Parameter(description = "字典数据信息", required = true)
            @Validated @RequestBody SysDictDataBo dict) {
        if (!dictDataService.checkDictDataUnique(dict)) {
            return R.fail("新增字典数据'" + dict.getDictValue() + "'失败，字典键值已存在");
        }
        dictDataService.insertDictData(dict);
        return R.ok();
    }

    /**
     * 修改保存字典类型
     */
    @SaCheckPermission("system:dict:edit")
    @Log(title = "字典数据", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改字典数据", description = "修改字典数据信息")
    public R<Void> edit(
            @Parameter(description = "字典数据信息", required = true)
            @Validated @RequestBody SysDictDataBo dict) {
        if (!dictDataService.checkDictDataUnique(dict)) {
            return R.fail("修改字典数据'" + dict.getDictValue() + "'失败，字典键值已存在");
        }
        dictDataService.updateDictData(dict);
        return R.ok();
    }

    /**
     * 删除字典类型
     *
     * @param dictCodes 字典code串
     */
    @SaCheckPermission("system:dict:remove")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictCodes}")
    @Operation(summary = "删除字典数据", description = "批量删除字典数据")
    public R<Void> remove(
            @Parameter(description = "字典代码数组", required = true)
            @PathVariable Long[] dictCodes) {
        dictDataService.deleteDictDataByIds(dictCodes);
        return R.ok();
    }
}
