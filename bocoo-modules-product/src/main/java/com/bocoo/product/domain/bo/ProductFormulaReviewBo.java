package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品配方审核查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "产品配方审核查询对象")
public class ProductFormulaReviewBo extends BaseBo {

    private String formulaCode;
    private String formulaName;
    private String versionLabel;
    private String submitBy;
    private String validationStatus;
}
