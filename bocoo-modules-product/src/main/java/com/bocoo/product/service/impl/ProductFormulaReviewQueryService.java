package com.bocoo.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mybatis.core.page.PageQuery;
import com.bocoo.common.mybatis.core.page.TableDataInfo;
import com.bocoo.product.domain.bo.ProductFormulaReviewBo;
import com.bocoo.product.domain.entity.ProductFormulaVersion;
import com.bocoo.product.domain.vo.ProductFormulaVersionVo;
import com.bocoo.product.mapper.ProductFormulaVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 产品配方审核列表查询。
 */
@Component
@RequiredArgsConstructor
public class ProductFormulaReviewQueryService extends ProductServiceSupport {

    private final ProductFormulaVersionMapper versionMapper;

    TableDataInfo<ProductFormulaVersionVo> queryReviewPage(ProductFormulaReviewBo bo, PageQuery pageQuery) {
        QueryWrapper<ProductFormulaVersion> q = activeQuery(ProductFormulaVersion.class)
            .eq("version_status", ProductFormulaServiceImpl.STATUS_PENDING_REVIEW);
        if (bo != null) {
            likeJsonText(q, "formulaCode", bo.getFormulaCode());
            likeJsonText(q, "formulaName", bo.getFormulaName());
            like(q, "version_label", bo.getVersionLabel());
            like(q, "submit_by", bo.getSubmitBy());
            eq(q, "validation_status", bo.getValidationStatus());
        }
        return page(versionMapper, pageQuery, q, query -> query.orderByDesc("submit_time", "version_id"));
    }

    private void likeJsonText(QueryWrapper<ProductFormulaVersion> q, String jsonKey, String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        q.apply("formula_snapshot_json::jsonb ->> '" + jsonKey + "' ILIKE {0}", "%" + value.trim() + "%");
    }
}
