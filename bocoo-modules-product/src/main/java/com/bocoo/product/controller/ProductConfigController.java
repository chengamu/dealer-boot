package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.*;
import com.bocoo.product.domain.vo.*;
import com.bocoo.product.service.ProductConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 产品配置模板接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品配置模板", description = "产品配置模板接口")
public class ProductConfigController extends BaseController {

    private final ProductConfigService productConfigService;

    @SaCheckPermission("product:sales-product:list")
    @GetMapping("/sales-products/list")
    @Operation(summary = "分页查询销售产品列表")
    public TableDataInfo<SalesProductVo> listSalesProduct(SalesProductBo bo, PageQuery pageQuery) {
        return productConfigService.querySalesProductPage(bo, pageQuery);
    }

    @SaCheckPermission("product:sales-product:list")
    @GetMapping("/sales-products/options")
    @Operation(summary = "查询销售产品选项")
    public R<java.util.List<SalesProductVo>> optionsSalesProduct(SalesProductBo bo) {
        return R.ok(productConfigService.querySalesProductList(bo));
    }

    @SaCheckPermission("product:sales-product:list")
    @GetMapping("/sales-products/{id}")
    @Operation(summary = "获取销售产品详情")
    public R<SalesProductVo> getSalesProduct(@PathVariable Long id) {
        return R.ok(productConfigService.getSalesProductById(id));
    }

    @SaCheckPermission("product:sales-product:add")
    @Log(title = "销售产品", businessType = BusinessType.INSERT)
    @PostMapping("/sales-products")
    @Operation(summary = "新增销售产品")
    public R<Void> addSalesProduct(@Validated @RequestBody SalesProductBo bo) {
        return toAjax(productConfigService.saveSalesProduct(bo));
    }

    @SaCheckPermission("product:sales-product:edit")
    @Log(title = "销售产品", businessType = BusinessType.UPDATE)
    @PutMapping("/sales-products")
    @Operation(summary = "修改销售产品")
    public R<Void> editSalesProduct(@Validated @RequestBody SalesProductBo bo) {
        return toAjax(productConfigService.saveSalesProduct(bo));
    }

    @SaCheckPermission("product:sales-product:edit")
    @Log(title = "修改销售产品状态", businessType = BusinessType.UPDATE)
    @PutMapping("/sales-products/change-status/{id}/{status}")
    @Operation(summary = "修改销售产品状态")
    public R<Void> changeSalesProductStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productConfigService.updateSalesProductStatus(id, status));
    }

    @SaCheckPermission("product:sales-product:remove")
    @Log(title = "销售产品", businessType = BusinessType.DELETE)
    @DeleteMapping("/sales-products/{ids}")
    @Operation(summary = "删除销售产品")
    public R<Void> removeSalesProduct(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productConfigService.removeSalesProductByIds(ids));
    }

    @SaCheckPermission("product:sales-product:reference")
    @GetMapping("/sales-products/{id}/references")
    @Operation(summary = "检查销售产品引用")
    public R<ReferenceCheckResultVo> checkSalesProductReferences(@PathVariable Long id) {
        return R.ok(productConfigService.checkSalesProductReferences(id));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/templates/list")
    @Operation(summary = "分页查询配置模板列表")
    public TableDataInfo<ConfigTemplateVo> listConfigTemplate(ConfigTemplateBo bo, PageQuery pageQuery) {
        return productConfigService.queryConfigTemplatePage(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/templates/options")
    @Operation(summary = "查询配置模板选项")
    public R<java.util.List<ConfigTemplateVo>> optionsConfigTemplate(ConfigTemplateBo bo) {
        return R.ok(productConfigService.queryConfigTemplateList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/templates/{id}")
    @Operation(summary = "获取配置模板详情")
    public R<ConfigTemplateVo> getConfigTemplate(@PathVariable Long id) {
        return R.ok(productConfigService.getConfigTemplateById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板", businessType = BusinessType.INSERT)
    @PostMapping("/templates")
    @Operation(summary = "新增配置模板")
    public R<Void> addConfigTemplate(@Validated @RequestBody ConfigTemplateBo bo) {
        return toAjax(productConfigService.saveConfigTemplate(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板", businessType = BusinessType.UPDATE)
    @PutMapping("/templates")
    @Operation(summary = "修改配置模板")
    public R<Void> editConfigTemplate(@Validated @RequestBody ConfigTemplateBo bo) {
        return toAjax(productConfigService.saveConfigTemplate(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/templates/{ids}")
    @Operation(summary = "删除配置模板")
    public R<Void> removeConfigTemplate(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productConfigService.removeConfigTemplateByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置模板状态", businessType = BusinessType.UPDATE)
    @PutMapping("/templates/change-status/{id}/{status}")
    @Operation(summary = "修改配置模板状态")
    public R<Void> changeConfigTemplateStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productConfigService.updateConfigTemplateStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/templates/{id}/references")
    @Operation(summary = "检查配置模板引用")
    public R<ReferenceCheckResultVo> checkConfigTemplateReferences(@PathVariable Long id) {
        return R.ok(productConfigService.checkConfigTemplateReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/template-versions/list")
    @Operation(summary = "分页查询配置模板版本列表")
    public TableDataInfo<ConfigTemplateVersionVo> listConfigTemplateVersion(ConfigTemplateVersionBo bo, PageQuery pageQuery) {
        return productConfigService.queryConfigTemplateVersionPage(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/template-versions/options")
    @Operation(summary = "查询配置模板版本选项")
    public R<java.util.List<ConfigTemplateVersionVo>> optionsConfigTemplateVersion(ConfigTemplateVersionBo bo) {
        return R.ok(productConfigService.queryConfigTemplateVersionList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/template-versions/{id}")
    @Operation(summary = "获取配置模板版本详情")
    public R<ConfigTemplateVersionVo> getConfigTemplateVersion(@PathVariable Long id) {
        return R.ok(productConfigService.getConfigTemplateVersionById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板版本", businessType = BusinessType.INSERT)
    @PostMapping("/template-versions")
    @Operation(summary = "新增配置模板版本")
    public R<Void> addConfigTemplateVersion(@Validated @RequestBody ConfigTemplateVersionBo bo) {
        return toAjax(productConfigService.saveConfigTemplateVersion(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板版本", businessType = BusinessType.UPDATE)
    @PutMapping("/template-versions")
    @Operation(summary = "修改配置模板版本")
    public R<Void> editConfigTemplateVersion(@Validated @RequestBody ConfigTemplateVersionBo bo) {
        return toAjax(productConfigService.saveConfigTemplateVersion(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置模板版本", businessType = BusinessType.DELETE)
    @DeleteMapping("/template-versions/{ids}")
    @Operation(summary = "删除配置模板版本")
    public R<Void> removeConfigTemplateVersion(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productConfigService.removeConfigTemplateVersionByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置模板版本状态", businessType = BusinessType.UPDATE)
    @PutMapping("/template-versions/change-status/{id}/{status}")
    @Operation(summary = "修改配置模板版本状态")
    public R<Void> changeConfigTemplateVersionStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productConfigService.updateConfigTemplateVersionStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/template-versions/{id}/references")
    @Operation(summary = "检查配置模板版本引用")
    public R<ReferenceCheckResultVo> checkConfigTemplateVersionReferences(@PathVariable Long id) {
        return R.ok(productConfigService.checkConfigTemplateVersionReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/question-groups/list")
    @Operation(summary = "分页查询配置问题组列表")
    public TableDataInfo<QuestionGroupVo> listQuestionGroup(QuestionGroupBo bo, PageQuery pageQuery) {
        return productConfigService.queryQuestionGroupPage(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/question-groups/options")
    @Operation(summary = "查询配置问题组选项")
    public R<java.util.List<QuestionGroupVo>> optionsQuestionGroup(QuestionGroupBo bo) {
        return R.ok(productConfigService.queryQuestionGroupList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/question-groups/{id}")
    @Operation(summary = "获取配置问题组详情")
    public R<QuestionGroupVo> getQuestionGroup(@PathVariable Long id) {
        return R.ok(productConfigService.getQuestionGroupById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题组", businessType = BusinessType.INSERT)
    @PostMapping("/question-groups")
    @Operation(summary = "新增配置问题组")
    public R<Void> addQuestionGroup(@Validated @RequestBody QuestionGroupBo bo) {
        return toAjax(productConfigService.saveQuestionGroup(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题组", businessType = BusinessType.UPDATE)
    @PutMapping("/question-groups")
    @Operation(summary = "修改配置问题组")
    public R<Void> editQuestionGroup(@Validated @RequestBody QuestionGroupBo bo) {
        return toAjax(productConfigService.saveQuestionGroup(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题组", businessType = BusinessType.DELETE)
    @DeleteMapping("/question-groups/{ids}")
    @Operation(summary = "删除配置问题组")
    public R<Void> removeQuestionGroup(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productConfigService.removeQuestionGroupByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置问题组状态", businessType = BusinessType.UPDATE)
    @PutMapping("/question-groups/change-status/{id}/{status}")
    @Operation(summary = "修改配置问题组状态")
    public R<Void> changeQuestionGroupStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productConfigService.updateQuestionGroupStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/question-groups/{id}/references")
    @Operation(summary = "检查配置问题组引用")
    public R<ReferenceCheckResultVo> checkQuestionGroupReferences(@PathVariable Long id) {
        return R.ok(productConfigService.checkQuestionGroupReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/questions/list")
    @Operation(summary = "分页查询配置问题列表")
    public TableDataInfo<ConfigQuestionVo> listConfigQuestion(ConfigQuestionBo bo, PageQuery pageQuery) {
        return productConfigService.queryConfigQuestionPage(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/questions/options")
    @Operation(summary = "查询配置问题选项")
    public R<java.util.List<ConfigQuestionVo>> optionsConfigQuestion(ConfigQuestionBo bo) {
        return R.ok(productConfigService.queryConfigQuestionList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/questions/{id}")
    @Operation(summary = "获取配置问题详情")
    public R<ConfigQuestionVo> getConfigQuestion(@PathVariable Long id) {
        return R.ok(productConfigService.getConfigQuestionById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题", businessType = BusinessType.INSERT)
    @PostMapping("/questions")
    @Operation(summary = "新增配置问题")
    public R<Void> addConfigQuestion(@Validated @RequestBody ConfigQuestionBo bo) {
        return toAjax(productConfigService.saveConfigQuestion(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题", businessType = BusinessType.UPDATE)
    @PutMapping("/questions")
    @Operation(summary = "修改配置问题")
    public R<Void> editConfigQuestion(@Validated @RequestBody ConfigQuestionBo bo) {
        return toAjax(productConfigService.saveConfigQuestion(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置问题", businessType = BusinessType.DELETE)
    @DeleteMapping("/questions/{ids}")
    @Operation(summary = "删除配置问题")
    public R<Void> removeConfigQuestion(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productConfigService.removeConfigQuestionByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置问题状态", businessType = BusinessType.UPDATE)
    @PutMapping("/questions/change-status/{id}/{status}")
    @Operation(summary = "修改配置问题状态")
    public R<Void> changeConfigQuestionStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productConfigService.updateConfigQuestionStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/questions/{id}/references")
    @Operation(summary = "检查配置问题引用")
    public R<ReferenceCheckResultVo> checkConfigQuestionReferences(@PathVariable Long id) {
        return R.ok(productConfigService.checkConfigQuestionReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/options/list")
    @Operation(summary = "分页查询配置答案选项列表")
    public TableDataInfo<ConfigOptionVo> listConfigOption(ConfigOptionBo bo, PageQuery pageQuery) {
        return productConfigService.queryConfigOptionPage(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/options/options")
    @Operation(summary = "查询配置答案选项选项")
    public R<java.util.List<ConfigOptionVo>> optionsConfigOption(ConfigOptionBo bo) {
        return R.ok(productConfigService.queryConfigOptionList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/options/{id}")
    @Operation(summary = "获取配置答案选项详情")
    public R<ConfigOptionVo> getConfigOption(@PathVariable Long id) {
        return R.ok(productConfigService.getConfigOptionById(id));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置答案选项", businessType = BusinessType.INSERT)
    @PostMapping("/options")
    @Operation(summary = "新增配置答案选项")
    public R<Void> addConfigOption(@Validated @RequestBody ConfigOptionBo bo) {
        return toAjax(productConfigService.saveConfigOption(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置答案选项", businessType = BusinessType.UPDATE)
    @PutMapping("/options")
    @Operation(summary = "修改配置答案选项")
    public R<Void> editConfigOption(@Validated @RequestBody ConfigOptionBo bo) {
        return toAjax(productConfigService.saveConfigOption(bo));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "配置答案选项", businessType = BusinessType.DELETE)
    @DeleteMapping("/options/{ids}")
    @Operation(summary = "删除配置答案选项")
    public R<Void> removeConfigOption(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productConfigService.removeConfigOptionByIds(ids));
    }

    @SaCheckPermission("product:template:edit")
    @Log(title = "修改配置答案状态", businessType = BusinessType.UPDATE)
    @PutMapping("/options/change-status/{id}/{status}")
    @Operation(summary = "修改配置答案状态")
    public R<Void> changeConfigOptionStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productConfigService.updateConfigOptionStatus(id, status));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/options/{id}/references")
    @Operation(summary = "检查配置答案引用")
    public R<ReferenceCheckResultVo> checkConfigOptionReferences(@PathVariable Long id) {
        return R.ok(productConfigService.checkConfigOptionReferences(id));
    }


    @SaCheckPermission("product:template:list")
    @GetMapping("/rules/list")
    @Operation(summary = "分页查询配置规则列表")
    public TableDataInfo<ConfigRuleVo> listConfigRule(ConfigRuleBo bo, PageQuery pageQuery) {
        return productConfigService.queryConfigRulePage(bo, pageQuery);
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/rules/options")
    @Operation(summary = "查询配置规则选项")
    public R<java.util.List<ConfigRuleVo>> optionsConfigRule(ConfigRuleBo bo) {
        return R.ok(productConfigService.queryConfigRuleList(bo));
    }

    @SaCheckPermission("product:template:list")
    @GetMapping("/rules/{id}")
    @Operation(summary = "获取配置规则详情")
    public R<ConfigRuleVo> getConfigRule(@PathVariable Long id) {
        return R.ok(productConfigService.getConfigRuleById(id));
    }

    @SaCheckPermission("product:template:rule")
    @Log(title = "配置规则", businessType = BusinessType.INSERT)
    @PostMapping("/rules")
    @Operation(summary = "新增配置规则")
    public R<Void> addConfigRule(@Validated @RequestBody ConfigRuleBo bo) {
        return toAjax(productConfigService.saveConfigRule(bo));
    }

    @SaCheckPermission("product:template:rule")
    @Log(title = "配置规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rules")
    @Operation(summary = "修改配置规则")
    public R<Void> editConfigRule(@Validated @RequestBody ConfigRuleBo bo) {
        return toAjax(productConfigService.saveConfigRule(bo));
    }

    @SaCheckPermission("product:template:rule")
    @Log(title = "配置规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/rules/{ids}")
    @Operation(summary = "删除配置规则")
    public R<Void> removeConfigRule(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(productConfigService.removeConfigRuleByIds(ids));
    }

    @SaCheckPermission("product:template:rule")
    @Log(title = "修改配置规则状态", businessType = BusinessType.UPDATE)
    @PutMapping("/rules/change-status/{id}/{status}")
    @Operation(summary = "修改配置规则状态")
    public R<Void> changeConfigRuleStatus(@PathVariable Long id, @PathVariable String status) {
        return toAjax(productConfigService.updateConfigRuleStatus(id, status));
    }

    @SaCheckPermission("product:template:test")
    @PostMapping("/config/evaluate")
    @Operation(summary = "配置求值")
    public R<ConfigEvaluationResultVo> evaluate(@RequestBody ConfigEvaluationBo bo) {
        return R.ok(productConfigService.evaluate(bo));
    }
}
