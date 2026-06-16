package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ConfigEvaluationBo;
import com.bocoo.product.domain.bo.ConfigOptionBo;
import com.bocoo.product.domain.bo.ConfigQuestionBo;
import com.bocoo.product.domain.bo.ConfigRuleBo;
import com.bocoo.product.domain.bo.ConfigTemplateBo;
import com.bocoo.product.domain.bo.ConfigTemplateVersionBo;
import com.bocoo.product.domain.bo.QuestionGroupBo;
import com.bocoo.product.domain.bo.SalesProductBo;
import com.bocoo.product.domain.vo.ConfigEvaluationResultVo;
import com.bocoo.product.domain.vo.ConfigOptionVo;
import com.bocoo.product.domain.vo.ConfigQuestionVo;
import com.bocoo.product.domain.vo.ConfigRuleVo;
import com.bocoo.product.domain.vo.ConfigTemplateVersionVo;
import com.bocoo.product.domain.vo.ConfigTemplateVo;
import com.bocoo.product.domain.vo.QuestionGroupVo;
import com.bocoo.product.domain.vo.ReferenceCheckResultVo;
import com.bocoo.product.domain.vo.SalesProductVo;
import com.bocoo.product.service.ConfigEvaluationService;
import com.bocoo.product.service.ConfigOptionService;
import com.bocoo.product.service.ConfigQuestionService;
import com.bocoo.product.service.ConfigRuleService;
import com.bocoo.product.service.ConfigTemplateService;
import com.bocoo.product.service.ConfigTemplateVersionService;
import com.bocoo.product.service.QuestionGroupService;
import com.bocoo.product.service.SalesProductService;
import io.swagger.v3.oas.annotations.Operation;
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

/**
 * 产品配置模板接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品配置模板", description = "产品配置模板接口")
public class ProductConfigController extends BaseController {

    private final SalesProductService salesProductService;
    private final ConfigTemplateService configTemplateService;
    private final ConfigTemplateVersionService configTemplateVersionService;
    private final QuestionGroupService questionGroupService;
    private final ConfigQuestionService configQuestionService;
    private final ConfigOptionService configOptionService;
    private final ConfigRuleService configRuleService;
    private final ConfigEvaluationService configEvaluationService;

    @SaCheckPermission("product:sales-product:list")
    @GetMapping("/sales-products/list")
    @Operation(summary = "分页查询销售产品列表")
    public TableDataInfo<SalesProductVo> listSalesProduct(SalesProductBo bo, PageQuery pageQuery) {
        return salesProductService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:sales-product:list")
    @GetMapping("/sales-products/options")
    @Operation(summary = "查询销售产品选项")
    public R<java.util.List<SalesProductVo>> optionsSalesProduct(SalesProductBo bo) {
        return R.ok(salesProductService.queryList(bo));
    }

    @SaCheckPermission("product:sales-product:list")
    @GetMapping("/sales-products/{id}")
    @Operation(summary = "获取销售产品详情")
    public R<SalesProductVo> getSalesProduct(@PathVariable Long id) {
        return R.ok(salesProductService.queryById(id));
    }

    @SaCheckPermission("product:sales-product:add")
    @Log(title = "销售产品", businessType = BusinessType.INSERT)
    @PostMapping("/sales-products")
    @Operation(summary = "新增销售产品")
    public R<Void> addSalesProduct(@Validated @RequestBody SalesProductBo bo) {
        return toAjax(salesProductService.insertByBo(bo));
    }

    @SaCheckPermission("product:sales-product:edit")
    @Log(title = "销售产品", businessType = BusinessType.UPDATE)
    @PutMapping("/sales-products")
    @Operation(summary = "修改销售产品")
    public R<Void> editSalesProduct(@Validated @RequestBody SalesProductBo bo) {
        return toAjax(salesProductService.updateByBo(bo));
    }

    @SaCheckPermission("product:sales-product:edit")
    @Log(title = "修改销售产品状态", businessType = BusinessType.UPDATE)
    @PutMapping("/sales-products/change-status/{id}/{status}")
    @Operation(summary = "修改销售产品状态")
    public R<Void> changeSalesProductStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(salesProductService.updateStatus(id, status));
    }

    @SaCheckPermission("product:sales-product:remove")
    @Log(title = "销售产品", businessType = BusinessType.DELETE)
    @DeleteMapping("/sales-products/{ids}")
    @Operation(summary = "删除销售产品")
    public R<Void> removeSalesProduct(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(salesProductService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:sales-product:reference")
    @GetMapping("/sales-products/{id}/references")
    @Operation(summary = "检查销售产品引用")
    public R<ReferenceCheckResultVo> checkSalesProductReferences(@PathVariable Long id) {
        return R.ok(salesProductService.checkReferences(id));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/templates/list")
    @Operation(summary = "分页查询配置模板列表")
    public TableDataInfo<ConfigTemplateVo> listConfigTemplate(ConfigTemplateBo bo, PageQuery pageQuery) {
        return configTemplateService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/templates/options")
    @Operation(summary = "查询配置模板选项")
    public R<java.util.List<ConfigTemplateVo>> optionsConfigTemplate(ConfigTemplateBo bo) {
        return R.ok(configTemplateService.queryList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/templates/{id}")
    @Operation(summary = "获取配置模板详情")
    public R<ConfigTemplateVo> getConfigTemplate(@PathVariable Long id) {
        return R.ok(configTemplateService.queryById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板", businessType = BusinessType.INSERT)
    @PostMapping("/templates")
    @Operation(summary = "新增配置模板")
    public R<Void> addConfigTemplate(@Validated @RequestBody ConfigTemplateBo bo) {
        return toAjax(configTemplateService.insertByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板", businessType = BusinessType.UPDATE)
    @PutMapping("/templates")
    @Operation(summary = "修改配置模板")
    public R<Void> editConfigTemplate(@Validated @RequestBody ConfigTemplateBo bo) {
        return toAjax(configTemplateService.updateByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/templates/{ids}")
    @Operation(summary = "删除配置模板")
    public R<Void> removeConfigTemplate(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(configTemplateService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置模板状态", businessType = BusinessType.UPDATE)
    @PutMapping("/templates/change-status/{id}/{status}")
    @Operation(summary = "修改配置模板状态")
    public R<Void> changeConfigTemplateStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(configTemplateService.updateStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/templates/{id}/references")
    @Operation(summary = "检查配置模板引用")
    public R<ReferenceCheckResultVo> checkConfigTemplateReferences(@PathVariable Long id) {
        return R.ok(configTemplateService.checkReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/template-versions/list")
    @Operation(summary = "分页查询配置模板版本列表")
    public TableDataInfo<ConfigTemplateVersionVo> listConfigTemplateVersion(ConfigTemplateVersionBo bo, PageQuery pageQuery) {
        return configTemplateVersionService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/template-versions/options")
    @Operation(summary = "查询配置模板版本选项")
    public R<java.util.List<ConfigTemplateVersionVo>> optionsConfigTemplateVersion(ConfigTemplateVersionBo bo) {
        return R.ok(configTemplateVersionService.queryList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/template-versions/{id}")
    @Operation(summary = "获取配置模板版本详情")
    public R<ConfigTemplateVersionVo> getConfigTemplateVersion(@PathVariable Long id) {
        return R.ok(configTemplateVersionService.queryById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板版本", businessType = BusinessType.INSERT)
    @PostMapping("/template-versions")
    @Operation(summary = "新增配置模板版本")
    public R<Void> addConfigTemplateVersion(@Validated @RequestBody ConfigTemplateVersionBo bo) {
        return toAjax(configTemplateVersionService.insertByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板版本", businessType = BusinessType.UPDATE)
    @PutMapping("/template-versions")
    @Operation(summary = "修改配置模板版本")
    public R<Void> editConfigTemplateVersion(@Validated @RequestBody ConfigTemplateVersionBo bo) {
        return toAjax(configTemplateVersionService.updateByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/template-versions/{ids}")
    @Operation(summary = "删除配置模板版本")
    public R<Void> removeConfigTemplateVersion(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(configTemplateVersionService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置模板版本状态", businessType = BusinessType.UPDATE)
    @PutMapping("/template-versions/change-status/{id}/{status}")
    @Operation(summary = "修改配置模板版本状态")
    public R<Void> changeConfigTemplateVersionStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(configTemplateVersionService.updateStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/template-versions/{id}/references")
    @Operation(summary = "检查配置模板版本引用")
    public R<ReferenceCheckResultVo> checkConfigTemplateVersionReferences(@PathVariable Long id) {
        return R.ok(configTemplateVersionService.checkReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/question-groups/list")
    @Operation(summary = "分页查询配置问题组列表")
    public TableDataInfo<QuestionGroupVo> listQuestionGroup(QuestionGroupBo bo, PageQuery pageQuery) {
        return questionGroupService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/question-groups/options")
    @Operation(summary = "查询配置问题组选项")
    public R<java.util.List<QuestionGroupVo>> optionsQuestionGroup(QuestionGroupBo bo) {
        return R.ok(questionGroupService.queryList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/question-groups/{id}")
    @Operation(summary = "获取配置问题组详情")
    public R<QuestionGroupVo> getQuestionGroup(@PathVariable Long id) {
        return R.ok(questionGroupService.queryById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题组", businessType = BusinessType.INSERT)
    @PostMapping("/question-groups")
    @Operation(summary = "新增配置问题组")
    public R<Void> addQuestionGroup(@Validated @RequestBody QuestionGroupBo bo) {
        return toAjax(questionGroupService.insertByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题组", businessType = BusinessType.UPDATE)
    @PutMapping("/question-groups")
    @Operation(summary = "修改配置问题组")
    public R<Void> editQuestionGroup(@Validated @RequestBody QuestionGroupBo bo) {
        return toAjax(questionGroupService.updateByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题组", businessType = BusinessType.DELETE)
    @DeleteMapping("/question-groups/{ids}")
    @Operation(summary = "删除配置问题组")
    public R<Void> removeQuestionGroup(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(questionGroupService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置问题组状态", businessType = BusinessType.UPDATE)
    @PutMapping("/question-groups/change-status/{id}/{status}")
    @Operation(summary = "修改配置问题组状态")
    public R<Void> changeQuestionGroupStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(questionGroupService.updateStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/question-groups/{id}/references")
    @Operation(summary = "检查配置问题组引用")
    public R<ReferenceCheckResultVo> checkQuestionGroupReferences(@PathVariable Long id) {
        return R.ok(questionGroupService.checkReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/questions/list")
    @Operation(summary = "分页查询配置问题列表")
    public TableDataInfo<ConfigQuestionVo> listConfigQuestion(ConfigQuestionBo bo, PageQuery pageQuery) {
        return configQuestionService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/questions/options")
    @Operation(summary = "查询配置问题选项")
    public R<java.util.List<ConfigQuestionVo>> optionsConfigQuestion(ConfigQuestionBo bo) {
        return R.ok(configQuestionService.queryList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/questions/{id}")
    @Operation(summary = "获取配置问题详情")
    public R<ConfigQuestionVo> getConfigQuestion(@PathVariable Long id) {
        return R.ok(configQuestionService.queryById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题", businessType = BusinessType.INSERT)
    @PostMapping("/questions")
    @Operation(summary = "新增配置问题")
    public R<Void> addConfigQuestion(@Validated @RequestBody ConfigQuestionBo bo) {
        return toAjax(configQuestionService.insertByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题", businessType = BusinessType.UPDATE)
    @PutMapping("/questions")
    @Operation(summary = "修改配置问题")
    public R<Void> editConfigQuestion(@Validated @RequestBody ConfigQuestionBo bo) {
        return toAjax(configQuestionService.updateByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题", businessType = BusinessType.DELETE)
    @DeleteMapping("/questions/{ids}")
    @Operation(summary = "删除配置问题")
    public R<Void> removeConfigQuestion(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(configQuestionService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置问题状态", businessType = BusinessType.UPDATE)
    @PutMapping("/questions/change-status/{id}/{status}")
    @Operation(summary = "修改配置问题状态")
    public R<Void> changeConfigQuestionStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(configQuestionService.updateStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/questions/{id}/references")
    @Operation(summary = "检查配置问题引用")
    public R<ReferenceCheckResultVo> checkConfigQuestionReferences(@PathVariable Long id) {
        return R.ok(configQuestionService.checkReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/options/list")
    @Operation(summary = "分页查询配置答案选项列表")
    public TableDataInfo<ConfigOptionVo> listConfigOption(ConfigOptionBo bo, PageQuery pageQuery) {
        return configOptionService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/options/options")
    @Operation(summary = "查询配置答案选项选项")
    public R<java.util.List<ConfigOptionVo>> optionsConfigOption(ConfigOptionBo bo) {
        return R.ok(configOptionService.queryList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/options/{id}")
    @Operation(summary = "获取配置答案选项详情")
    public R<ConfigOptionVo> getConfigOption(@PathVariable Long id) {
        return R.ok(configOptionService.queryById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置答案选项", businessType = BusinessType.INSERT)
    @PostMapping("/options")
    @Operation(summary = "新增配置答案选项")
    public R<Void> addConfigOption(@Validated @RequestBody ConfigOptionBo bo) {
        return toAjax(configOptionService.insertByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置答案选项", businessType = BusinessType.UPDATE)
    @PutMapping("/options")
    @Operation(summary = "修改配置答案选项")
    public R<Void> editConfigOption(@Validated @RequestBody ConfigOptionBo bo) {
        return toAjax(configOptionService.updateByBo(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置答案选项", businessType = BusinessType.DELETE)
    @DeleteMapping("/options/{ids}")
    @Operation(summary = "删除配置答案选项")
    public R<Void> removeConfigOption(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(configOptionService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置答案状态", businessType = BusinessType.UPDATE)
    @PutMapping("/options/change-status/{id}/{status}")
    @Operation(summary = "修改配置答案状态")
    public R<Void> changeConfigOptionStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(configOptionService.updateStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/options/{id}/references")
    @Operation(summary = "检查配置答案引用")
    public R<ReferenceCheckResultVo> checkConfigOptionReferences(@PathVariable Long id) {
        return R.ok(configOptionService.checkReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/rules/list")
    @Operation(summary = "分页查询配置规则列表")
    public TableDataInfo<ConfigRuleVo> listConfigRule(ConfigRuleBo bo, PageQuery pageQuery) {
        return configRuleService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/rules/options")
    @Operation(summary = "查询配置规则选项")
    public R<java.util.List<ConfigRuleVo>> optionsConfigRule(ConfigRuleBo bo) {
        return R.ok(configRuleService.queryList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/rules/{id}")
    @Operation(summary = "获取配置规则详情")
    public R<ConfigRuleVo> getConfigRule(@PathVariable Long id) {
        return R.ok(configRuleService.queryById(id));
    }

    @SaCheckPermission("product:template:rule")
    @Log(title = "配置规则", businessType = BusinessType.INSERT)
    @PostMapping("/rules")
    @Operation(summary = "新增配置规则")
    public R<Void> addConfigRule(@Validated @RequestBody ConfigRuleBo bo) {
        return toAjax(configRuleService.insertByBo(bo));
    }

    @SaCheckPermission("product:template:rule")
    @Log(title = "配置规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rules")
    @Operation(summary = "修改配置规则")
    public R<Void> editConfigRule(@Validated @RequestBody ConfigRuleBo bo) {
        return toAjax(configRuleService.updateByBo(bo));
    }

    @SaCheckPermission("product:template:rule")
    @Log(title = "配置规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/rules/{ids}")
    @Operation(summary = "删除配置规则")
    public R<Void> removeConfigRule(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(configRuleService.deleteWithValidByIds(ids));
    }

    @SaCheckPermission("product:template:rule")
    @Log(title = "修改配置规则状态", businessType = BusinessType.UPDATE)
    @PutMapping("/rules/change-status/{id}/{status}")
    @Operation(summary = "修改配置规则状态")
    public R<Void> changeConfigRuleStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(configRuleService.updateStatus(id, status));
    }

    @SaCheckPermission("product:template:test")
    @PostMapping("/config/evaluate")
    @Operation(summary = "配置求值")
    public R<ConfigEvaluationResultVo> evaluate(@RequestBody ConfigEvaluationBo bo) {
        return R.ok(configEvaluationService.evaluate(bo));
    }
}
