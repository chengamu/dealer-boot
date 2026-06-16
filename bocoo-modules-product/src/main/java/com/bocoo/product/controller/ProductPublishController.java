package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductPublishPackageBo;
import com.bocoo.product.domain.bo.ProductSyncOutboxBo;
import com.bocoo.product.domain.bo.PublishApprovalBo;
import com.bocoo.product.domain.bo.PublishCheckBo;
import com.bocoo.product.domain.bo.PublishCheckResultBo;
import com.bocoo.product.domain.vo.ProductPublishPackageVo;
import com.bocoo.product.domain.vo.ProductSyncOutboxVo;
import com.bocoo.product.domain.vo.PublishApprovalVo;
import com.bocoo.product.domain.vo.PublishCheckResultVo;
import com.bocoo.product.domain.vo.PublishCheckSummaryVo;
import com.bocoo.product.domain.vo.PublishExecutionResultVo;
import com.bocoo.product.service.ProductPublishPackageService;
import com.bocoo.product.service.ProductPublishWorkflowService;
import com.bocoo.product.service.ProductSyncOutboxService;
import com.bocoo.product.service.PublishApprovalService;
import com.bocoo.product.service.PublishCheckResultService;
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
 * 产品发布接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability")
@Tag(name = "产品发布", description = "产品发布接口")
public class ProductPublishController extends BaseController {

    private final PublishCheckResultService publishCheckResultService;
    private final PublishApprovalService publishApprovalService;
    private final ProductPublishPackageService productPublishPackageService;
    private final ProductSyncOutboxService productSyncOutboxService;
    private final ProductPublishWorkflowService productPublishWorkflowService;

    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-check-results/list")
    @Operation(summary = "分页查询发布检查结果列表")
    public TableDataInfo<PublishCheckResultVo> listPublishCheckResult(PublishCheckResultBo bo, PageQuery pageQuery) {
        return publishCheckResultService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-check-results/options")
    @Operation(summary = "查询发布检查结果选项")
    public R<java.util.List<PublishCheckResultVo>> optionsPublishCheckResult(PublishCheckResultBo bo) {
        return R.ok(publishCheckResultService.queryList(bo));
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-check-results/{id}")
    @Operation(summary = "获取发布检查结果详情")
    public R<PublishCheckResultVo> getPublishCheckResult(@PathVariable Long id) {
        return R.ok(publishCheckResultService.queryById(id));
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/gap-tasks/list")
    @Operation(summary = "分页查询发布缺口待办列表")
    public TableDataInfo<PublishCheckResultVo> listGapTask(PublishCheckResultBo bo, PageQuery pageQuery) {
        return publishCheckResultService.queryGapTaskPage(bo, pageQuery);
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/gap-tasks/{id}")
    @Operation(summary = "获取发布缺口待办详情")
    public R<PublishCheckResultVo> getGapTask(@PathVariable Long id) {
        return R.ok(publishCheckResultService.queryById(id));
    }

    @SaCheckPermission("product:publish:resolve")
    @Log(title = "发布缺口待办处理", businessType = BusinessType.UPDATE)
    @PostMapping("/gap-tasks/{id}/resolve")
    @Operation(summary = "标记发布缺口待办已处理")
    public R<Void> resolveGapTask(@PathVariable Long id) {
        return toAjax(publishCheckResultService.resolveGapTask(id));
    }

    @SaCheckPermission("product:publish:approve")
    @Log(title = "发布检查结果", businessType = BusinessType.INSERT)
    @PostMapping("/publish-check-results")
    @Operation(summary = "新增发布检查结果")
    public R<Void> addPublishCheckResult(@Validated @RequestBody PublishCheckResultBo bo) {
        return toAjax(publishCheckResultService.save(bo));
    }

    @SaCheckPermission("product:publish:approve")
    @Log(title = "发布检查结果", businessType = BusinessType.UPDATE)
    @PutMapping("/publish-check-results")
    @Operation(summary = "修改发布检查结果")
    public R<Void> editPublishCheckResult(@Validated @RequestBody PublishCheckResultBo bo) {
        return toAjax(publishCheckResultService.save(bo));
    }

    @SaCheckPermission("product:publish:approve")
    @Log(title = "发布检查结果", businessType = BusinessType.DELETE)
    @DeleteMapping("/publish-check-results/{ids}")
    @Operation(summary = "删除发布检查结果")
    public R<Void> removePublishCheckResult(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(publishCheckResultService.deleteByIds(ids));
    }


    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-approvals/list")
    @Operation(summary = "分页查询发布审批记录列表")
    public TableDataInfo<PublishApprovalVo> listPublishApproval(PublishApprovalBo bo, PageQuery pageQuery) {
        return publishApprovalService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-approvals/options")
    @Operation(summary = "查询发布审批记录选项")
    public R<java.util.List<PublishApprovalVo>> optionsPublishApproval(PublishApprovalBo bo) {
        return R.ok(publishApprovalService.queryList(bo));
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-approvals/{id}")
    @Operation(summary = "获取发布审批记录详情")
    public R<PublishApprovalVo> getPublishApproval(@PathVariable Long id) {
        return R.ok(publishApprovalService.queryById(id));
    }

    @SaCheckPermission("product:publish:approve")
    @Log(title = "发布审批记录", businessType = BusinessType.INSERT)
    @PostMapping("/publish-approvals")
    @Operation(summary = "新增发布审批记录")
    public R<Void> addPublishApproval(@Validated @RequestBody PublishApprovalBo bo) {
        return toAjax(publishApprovalService.save(bo));
    }

    @SaCheckPermission("product:publish:submit")
    @Log(title = "发布提交审批", businessType = BusinessType.INSERT)
    @PostMapping("/publish-approvals/submit")
    @Operation(summary = "提交产品发布审批")
    public R<PublishApprovalVo> submitPublishApproval(@RequestBody PublishCheckBo bo) {
        return R.ok(publishApprovalService.submit(bo));
    }

    @SaCheckPermission("product:publish:approve")
    @Log(title = "发布审批记录", businessType = BusinessType.UPDATE)
    @PutMapping("/publish-approvals")
    @Operation(summary = "修改发布审批记录")
    public R<Void> editPublishApproval(@Validated @RequestBody PublishApprovalBo bo) {
        return toAjax(publishApprovalService.save(bo));
    }

    @SaCheckPermission("product:publish:approve")
    @Log(title = "发布审批记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/publish-approvals/{ids}")
    @Operation(summary = "删除发布审批记录")
    public R<Void> removePublishApproval(@NotEmpty(message = "{gen.validation.pk.required}") @PathVariable Long[] ids) {
        return toAjax(publishApprovalService.deleteByIds(ids));
    }

    @SaCheckPermission("product:publish:approve")
    @Log(title = "发布审批通过", businessType = BusinessType.UPDATE)
    @PostMapping("/publish-approvals/{id}/approve")
    @Operation(summary = "审批通过发布记录")
    public R<Void> approvePublishApproval(@PathVariable Long id, @RequestBody(required = false) PublishApprovalBo bo) {
        return toAjax(publishApprovalService.updateApprovalStatus(id, "APPROVED", bo == null ? null : bo.getApprovalComment()));
    }

    @SaCheckPermission("product:publish:reject")
    @Log(title = "发布审批拒绝", businessType = BusinessType.UPDATE)
    @PostMapping("/publish-approvals/{id}/reject")
    @Operation(summary = "审批拒绝发布记录")
    public R<Void> rejectPublishApproval(@PathVariable Long id, @RequestBody(required = false) PublishApprovalBo bo) {
        return toAjax(publishApprovalService.updateApprovalStatus(id, "REJECTED", bo == null ? null : bo.getApprovalComment()));
    }


    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-packages/list")
    @Operation(summary = "分页查询产品发布包列表")
    public TableDataInfo<ProductPublishPackageVo> listProductPublishPackage(ProductPublishPackageBo bo, PageQuery pageQuery) {
        return productPublishPackageService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-packages/options")
    @Operation(summary = "查询产品发布包选项")
    public R<java.util.List<ProductPublishPackageVo>> optionsProductPublishPackage(ProductPublishPackageBo bo) {
        return R.ok(productPublishPackageService.queryList(bo));
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/publish-packages/{id}")
    @Operation(summary = "获取产品发布包详情")
    public R<ProductPublishPackageVo> getProductPublishPackage(@PathVariable Long id) {
        return R.ok(productPublishPackageService.queryById(id));
    }

    @SaCheckPermission("product:sales-view:list")
    @GetMapping("/sales-view/publish-packages/list")
    @Operation(summary = "分页查询销售只读产品发布包列表")
    public TableDataInfo<ProductPublishPackageVo> listSalesViewProductPublishPackage(ProductPublishPackageBo bo, PageQuery pageQuery) {
        if (bo == null) {
            bo = new ProductPublishPackageBo();
        }
        bo.setPackageStatus("PUBLISHED");
        return productPublishPackageService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:sales-view:list")
    @GetMapping("/sales-view/publish-packages/{id}")
    @Operation(summary = "获取销售只读产品发布包详情")
    public R<ProductPublishPackageVo> getSalesViewProductPublishPackage(@PathVariable Long id) {
        return R.ok(productPublishPackageService.queryById(id));
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/sync-outbox/list")
    @Operation(summary = "分页查询产品能力同步Outbox列表")
    public TableDataInfo<ProductSyncOutboxVo> listProductSyncOutbox(ProductSyncOutboxBo bo, PageQuery pageQuery) {
        return productSyncOutboxService.queryPageList(bo, pageQuery);
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/sync-outbox/options")
    @Operation(summary = "查询产品能力同步Outbox选项")
    public R<java.util.List<ProductSyncOutboxVo>> optionsProductSyncOutbox(ProductSyncOutboxBo bo) {
        return R.ok(productSyncOutboxService.queryList(bo));
    }

    @SaCheckPermission("product:publish:list")
    @GetMapping("/sync-outbox/{id}")
    @Operation(summary = "获取产品能力同步Outbox详情")
    public R<ProductSyncOutboxVo> getProductSyncOutbox(@PathVariable Long id) {
        return R.ok(productSyncOutboxService.queryById(id));
    }

    @SaCheckPermission("product:publish:retrySync")
    @Log(title = "产品能力同步Outbox重试", businessType = BusinessType.SENSITIVE_OPERATION)
    @PostMapping("/sync-outbox/{id}/retry")
    @Operation(summary = "标记产品能力同步Outbox为待重试")
    public R<Void> retryProductSyncOutbox(@PathVariable Long id) {
        return toAjax(productSyncOutboxService.retry(id));
    }

    @SaCheckPermission("product:publish:check")
    @PostMapping("/publish/check")
    @Operation(summary = "执行发布检查")
    public R<PublishCheckSummaryVo> check(@RequestBody PublishCheckBo bo) {
        return R.ok(productPublishWorkflowService.check(bo));
    }

    @SaCheckPermission("product:publish:publish")
    @Log(title = "产品发布包", businessType = BusinessType.INSERT)
    @PostMapping("/publish/packages/create")
    @Operation(summary = "生成产品发布包")
    public R<PublishExecutionResultVo> publish(@RequestBody PublishCheckBo bo) {
        return R.ok(productPublishWorkflowService.publish(bo));
    }
}
