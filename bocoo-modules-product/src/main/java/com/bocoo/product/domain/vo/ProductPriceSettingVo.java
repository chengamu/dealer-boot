package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductPriceSetting;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = ProductPriceSetting.class)
@Schema(description = "产品价格设置视图对象")
public class ProductPriceSettingVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
    private LocalDateTime validationTime;
    private String status;
    private String delFlag;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
