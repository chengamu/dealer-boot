package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.core.domain.R;
import com.bocoo.common.log.annotation.Log;
import com.bocoo.common.log.enums.BusinessType;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.common.web.core.BaseController;
import com.bocoo.product.domain.bo.ProductFormulaRejectBo;
import com.bocoo.product.domain.bo.ProductFormulaReviewBo;
import com.bocoo.product.domain.vo.ProductFormulaReviewRecordVo;
import com.bocoo.product.domain.vo.ProductFormulaVersionVo;
import com.bocoo.product.service.ProductFormulaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-formula/reviews")
@Tag(name = "产品配方审核", description = "配方审核候选快照接口")
public class ProductFormulaReviewController extends BaseController {

    private final ProductFormulaService formulaService;

    @SaCheckPermission("product:formula:review")
    @GetMapping("/list")
    @Operation(summary = "分页查询待审核配方")
    public TableDataInfo<ProductFormulaVersionVo> list(ProductFormulaReviewBo bo, PageQuery pageQuery) {
        return formulaService.queryReviewPage(bo, pageQuery);
    }

    @SaCheckPermission("product:formula:review")
    @GetMapping("/{reviewId}")
    @Operation(summary = "获取配方审核详情")
    public R<ProductFormulaVersionVo> get(@PathVariable Long reviewId) {
        return R.ok(formulaService.queryReviewById(reviewId));
    }

    @SaCheckPermission("product:formula:reference")
    @GetMapping("/{reviewId}/records")
    @Operation(summary = "查询配方审核记录")
    public R<List<ProductFormulaReviewRecordVo>> records(@PathVariable Long reviewId) {
        return R.ok(formulaService.queryReviewRecords(reviewId));
    }

    @SaCheckPermission("product:formula:approve")
    @Log(title = "审核通过配方", businessType = BusinessType.UPDATE)
    @PutMapping("/{reviewId}/approve")
    @Operation(summary = "审核通过配方")
    public R<Void> approve(@PathVariable Long reviewId) {
        return toAjax(formulaService.approveReview(reviewId));
    }

    @SaCheckPermission("product:formula:reject")
    @Log(title = "驳回配方", businessType = BusinessType.UPDATE)
    @PutMapping("/{reviewId}/reject")
    @Operation(summary = "驳回配方")
    public R<Void> reject(@PathVariable Long reviewId, @Validated @RequestBody ProductFormulaRejectBo bo) {
        return toAjax(formulaService.rejectReview(reviewId, bo.getRejectReason()));
    }
}
