package com.bocoo.product.controller;

import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.EngineeringCheckCaseBo;
import com.bocoo.product.domain.bo.EngineeringItemBo;
import com.bocoo.product.domain.bo.EngineeringItemScopeBo;
import com.bocoo.product.domain.bo.EngineeringOutputRuleBo;
import com.bocoo.product.domain.bo.EngineeringPlanBo;
import com.bocoo.product.domain.bo.EngineeringPlanVersionBo;
import com.bocoo.product.domain.bo.EngineeringRuleBo;
import com.bocoo.product.domain.bo.StandardSkuEngineeringBo;
import com.bocoo.product.domain.vo.EngineeringCheckCaseVo;
import com.bocoo.product.domain.vo.EngineeringItemScopeVo;
import com.bocoo.product.domain.vo.EngineeringItemVo;
import com.bocoo.product.domain.vo.EngineeringOutputRuleVo;
import com.bocoo.product.domain.vo.EngineeringPlanVersionVo;
import com.bocoo.product.domain.vo.EngineeringPlanVo;
import com.bocoo.product.domain.vo.EngineeringRuleVo;
import com.bocoo.product.domain.vo.StandardSkuEngineeringVo;
import com.bocoo.product.service.EngineeringCheckCaseService;
import com.bocoo.product.service.EngineeringCheckService;
import com.bocoo.product.service.EngineeringItemScopeService;
import com.bocoo.product.service.EngineeringItemService;
import com.bocoo.product.service.EngineeringOutputRuleService;
import com.bocoo.product.service.EngineeringPlanService;
import com.bocoo.product.service.EngineeringPlanVersionService;
import com.bocoo.product.service.EngineeringPreviewService;
import com.bocoo.product.service.EngineeringRuleService;
import com.bocoo.product.service.EngineeringWorkbenchService;
import com.bocoo.product.service.StandardSkuEngineeringService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping({"/product-capability/engineering", "/product-engineering"})
@Tag(name = "产品工程设计")
public class ProductEngineeringController extends BaseController {

    private final EngineeringPlanService engineeringPlanService;
    private final EngineeringPlanVersionService engineeringPlanVersionService;
    private final EngineeringItemService engineeringItemService;
    private final EngineeringItemScopeService engineeringItemScopeService;
    private final EngineeringRuleService engineeringRuleService;
    private final EngineeringOutputRuleService engineeringOutputRuleService;
    private final StandardSkuEngineeringService standardSkuEngineeringService;
    private final EngineeringCheckCaseService engineeringCheckCaseService;
    private final EngineeringWorkbenchService engineeringWorkbenchService;
    private final EngineeringPreviewService engineeringPreviewService;
    private final EngineeringCheckService engineeringCheckService;

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/plans/list")
    public TableDataInfo<EngineeringPlanVo> listPlans(EngineeringPlanBo query, PageQuery pageQuery) {
        return engineeringPlanService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/plans/options")
    public R<List<EngineeringPlanVo>> planOptions(EngineeringPlanBo query) {
        return R.ok(engineeringPlanService.queryList(query));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/plans/{id}")
    public R<EngineeringPlanVo> getPlan(@PathVariable Long id) {
        return R.ok(engineeringPlanService.queryById(id));
    }

    @SaCheckPermission("product:engineering:add")
    @Log(title = "工程方案", businessType = BusinessType.INSERT)
    @PostMapping("/plans")
    public R<Void> addPlan(@RequestBody EngineeringPlanBo bo) {
        return toAjax(engineeringPlanService.save(bo));
    }

    @SaCheckPermission("product:engineering:edit")
    @Log(title = "工程方案", businessType = BusinessType.UPDATE)
    @PutMapping("/plans")
    public R<Void> editPlan(@RequestBody EngineeringPlanBo bo) {
        return toAjax(engineeringPlanService.save(bo));
    }

    @SaCheckPermission("product:engineering:remove")
    @Log(title = "工程方案", businessType = BusinessType.DELETE)
    @DeleteMapping("/plans/{ids}")
    public R<Void> removePlans(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(engineeringPlanService.deleteByIds(ids));
    }

    @SaCheckPermission("product:engineering:edit")
    @PutMapping("/plans/change-status/{id}/{status}")
    public R<Void> changePlanStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(engineeringPlanService.updateStatus(id, status));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/workbench/{versionId}")
    public R<Map<String, Object>> workbench(@PathVariable Long versionId) {
        return R.ok(engineeringWorkbenchService.load(versionId));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/versions/list")
    public TableDataInfo<EngineeringPlanVersionVo> listVersions(EngineeringPlanVersionBo query, PageQuery pageQuery) {
        return engineeringPlanVersionService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/versions/{id}")
    public R<EngineeringPlanVersionVo> getVersion(@PathVariable Long id) {
        return R.ok(engineeringPlanVersionService.queryById(id));
    }

    @SaCheckPermission("product:engineering:add")
    @PostMapping("/versions")
    public R<Void> saveVersion(@RequestBody EngineeringPlanVersionBo bo) {
        return toAjax(engineeringPlanVersionService.save(bo));
    }

    @SaCheckPermission("product:engineering:edit")
    @PutMapping("/versions")
    public R<Void> editVersion(@RequestBody EngineeringPlanVersionBo bo) {
        return toAjax(engineeringPlanVersionService.save(bo));
    }

    @SaCheckPermission("product:engineering:remove")
    @DeleteMapping("/versions/{ids}")
    public R<Void> removeVersions(@PathVariable Long[] ids) {
        return toAjax(engineeringPlanVersionService.deleteByIds(ids));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/items/list")
    public TableDataInfo<EngineeringItemVo> listItems(EngineeringItemBo query, PageQuery pageQuery) {
        return engineeringItemService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/items/{id}")
    public R<EngineeringItemVo> getItem(@PathVariable Long id) {
        return R.ok(engineeringItemService.queryById(id));
    }

    @SaCheckPermission("product:engineering:add")
    @PostMapping("/items")
    public R<Void> saveItem(@RequestBody EngineeringItemBo bo) {
        return toAjax(engineeringItemService.save(bo));
    }

    @SaCheckPermission("product:engineering:edit")
    @PutMapping("/items")
    public R<Void> editItem(@RequestBody EngineeringItemBo bo) {
        return toAjax(engineeringItemService.save(bo));
    }

    @SaCheckPermission("product:engineering:remove")
    @DeleteMapping("/items/{ids}")
    public R<Void> removeItems(@PathVariable Long[] ids) {
        return toAjax(engineeringItemService.deleteByIds(ids));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/scopes/list")
    public TableDataInfo<EngineeringItemScopeVo> listScopes(EngineeringItemScopeBo query, PageQuery pageQuery) {
        return engineeringItemScopeService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/scopes/{id}")
    public R<EngineeringItemScopeVo> getScope(@PathVariable Long id) {
        return R.ok(engineeringItemScopeService.queryById(id));
    }

    @SaCheckPermission("product:engineering:add")
    @PostMapping("/scopes")
    public R<Void> saveScope(@RequestBody EngineeringItemScopeBo bo) {
        return toAjax(engineeringItemScopeService.save(bo));
    }

    @SaCheckPermission("product:engineering:edit")
    @PutMapping("/scopes")
    public R<Void> editScope(@RequestBody EngineeringItemScopeBo bo) {
        return toAjax(engineeringItemScopeService.save(bo));
    }

    @SaCheckPermission("product:engineering:remove")
    @DeleteMapping("/scopes/{ids}")
    public R<Void> removeScopes(@PathVariable Long[] ids) {
        return toAjax(engineeringItemScopeService.deleteByIds(ids));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/rules/list")
    public TableDataInfo<EngineeringRuleVo> listRules(EngineeringRuleBo query, PageQuery pageQuery) {
        return engineeringRuleService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/rules/{id}")
    public R<EngineeringRuleVo> getRule(@PathVariable Long id) {
        return R.ok(engineeringRuleService.queryById(id));
    }

    @SaCheckPermission("product:engineering:add")
    @PostMapping("/rules")
    public R<Void> saveRule(@RequestBody EngineeringRuleBo bo) {
        return toAjax(engineeringRuleService.save(bo));
    }

    @SaCheckPermission("product:engineering:edit")
    @PutMapping("/rules")
    public R<Void> editRule(@RequestBody EngineeringRuleBo bo) {
        return toAjax(engineeringRuleService.save(bo));
    }

    @SaCheckPermission("product:engineering:remove")
    @DeleteMapping("/rules/{ids}")
    public R<Void> removeRules(@PathVariable Long[] ids) {
        return toAjax(engineeringRuleService.deleteByIds(ids));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/output-rules/list")
    public TableDataInfo<EngineeringOutputRuleVo> listOutputRules(EngineeringOutputRuleBo query, PageQuery pageQuery) {
        return engineeringOutputRuleService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/output-rules/{id}")
    public R<EngineeringOutputRuleVo> getOutputRule(@PathVariable Long id) {
        return R.ok(engineeringOutputRuleService.queryById(id));
    }

    @SaCheckPermission("product:engineering:add")
    @PostMapping("/output-rules")
    public R<Void> saveOutputRule(@RequestBody EngineeringOutputRuleBo bo) {
        return toAjax(engineeringOutputRuleService.save(bo));
    }

    @SaCheckPermission("product:engineering:edit")
    @PutMapping("/output-rules")
    public R<Void> editOutputRule(@RequestBody EngineeringOutputRuleBo bo) {
        return toAjax(engineeringOutputRuleService.save(bo));
    }

    @SaCheckPermission("product:engineering:remove")
    @DeleteMapping("/output-rules/{ids}")
    public R<Void> removeOutputRules(@PathVariable Long[] ids) {
        return toAjax(engineeringOutputRuleService.deleteByIds(ids));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/standard-skus/list")
    public TableDataInfo<StandardSkuEngineeringVo> listStandardSkuEngineering(StandardSkuEngineeringBo query, PageQuery pageQuery) {
        return standardSkuEngineeringService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/standard-skus/{id}")
    public R<StandardSkuEngineeringVo> getStandardSkuEngineering(@PathVariable Long id) {
        return R.ok(standardSkuEngineeringService.queryById(id));
    }

    @SaCheckPermission("product:engineering:edit")
    @PostMapping("/standard-skus")
    public R<Void> saveStandardSkuEngineering(@RequestBody StandardSkuEngineeringBo bo) {
        return toAjax(standardSkuEngineeringService.save(bo));
    }

    @SaCheckPermission("product:engineering:edit")
    @PutMapping("/standard-skus")
    public R<Void> editStandardSkuEngineering(@RequestBody StandardSkuEngineeringBo bo) {
        return toAjax(standardSkuEngineeringService.save(bo));
    }

    @SaCheckPermission("product:engineering:remove")
    @DeleteMapping("/standard-skus/{ids}")
    public R<Void> removeStandardSkuEngineering(@PathVariable Long[] ids) {
        return toAjax(standardSkuEngineeringService.deleteByIds(ids));
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/check-cases/list")
    public TableDataInfo<EngineeringCheckCaseVo> listCheckCases(EngineeringCheckCaseBo query, PageQuery pageQuery) {
        return engineeringCheckCaseService.queryPageList(query, pageQuery);
    }

    @SaCheckPermission("product:engineering:list")
    @GetMapping("/check-cases/{id}")
    public R<EngineeringCheckCaseVo> getCheckCase(@PathVariable Long id) {
        return R.ok(engineeringCheckCaseService.queryById(id));
    }

    @SaCheckPermission("product:engineering:edit")
    @PostMapping("/check-cases")
    public R<Void> saveCheckCase(@RequestBody EngineeringCheckCaseBo bo) {
        return toAjax(engineeringCheckCaseService.save(bo));
    }

    @SaCheckPermission("product:engineering:edit")
    @PutMapping("/check-cases")
    public R<Void> editCheckCase(@RequestBody EngineeringCheckCaseBo bo) {
        return toAjax(engineeringCheckCaseService.save(bo));
    }

    @SaCheckPermission("product:engineering:remove")
    @DeleteMapping("/check-cases/{ids}")
    public R<Void> removeCheckCases(@PathVariable Long[] ids) {
        return toAjax(engineeringCheckCaseService.deleteByIds(ids));
    }

    @SaCheckPermission("product:engineering:preview")
    @PostMapping("/preview")
    public R<Map<String, Object>> preview(@RequestBody Map<String, Object> input) {
        return R.ok(engineeringPreviewService.preview(input));
    }

    @SaCheckPermission("product:engineering:check")
    @GetMapping("/check/{versionId}")
    public R<Map<String, Object>> check(@PathVariable Long versionId) {
        return R.ok(engineeringCheckService.check(versionId));
    }
}
