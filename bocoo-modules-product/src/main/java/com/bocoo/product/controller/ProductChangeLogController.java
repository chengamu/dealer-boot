package com.bocoo.product.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductChangeLogBo;
import com.bocoo.product.domain.vo.ProductChangeLogVo;
import com.bocoo.product.service.ProductChangeLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 产品业务变更流水接口。
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/product-capability/change-logs")
@Tag(name = "产品业务变更流水", description = "产品域业务对象变更流水查询接口")
public class ProductChangeLogController {

    private final ProductChangeLogService changeLogService;

    @SaCheckPermission("product:base:reference")
    @GetMapping("/list")
    @Operation(summary = "分页查询产品业务变更流水")
    public TableDataInfo<ProductChangeLogVo> list(ProductChangeLogBo bo, PageQuery pageQuery) {
        return changeLogService.queryPageList(bo, pageQuery);
    }
}
