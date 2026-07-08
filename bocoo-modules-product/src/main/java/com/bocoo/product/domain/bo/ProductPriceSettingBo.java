package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductPriceSetting;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品价格设置业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductPriceSetting.class, reverseConvertGenerate = false)
@Schema(description = "产品价格设置业务对象")
public class ProductPriceSettingBo extends BaseBo {
    private Long priceSettingId;
    private Long tenantId;
    private Long saleProductId;
    private String saleProductCode;
    private String saleProductName;
    private Long formulaId;
    private Long formulaVersionId;
    private String formulaVersionLabel;
    private String currencyCode;
    private String validationStatus;
    private String validationMessage;
    private java.time.LocalDateTime validationTime;
    private String status;
    private String delFlag;
    private String remark;
}
