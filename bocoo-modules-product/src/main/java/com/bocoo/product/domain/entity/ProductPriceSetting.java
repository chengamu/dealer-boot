package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 产品价格设置 pc_price_setting
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_price_setting")
@Schema(description = "产品价格设置")
public class ProductPriceSetting extends BaseEntity {

    @TableId(value = "price_setting_id")
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
}
