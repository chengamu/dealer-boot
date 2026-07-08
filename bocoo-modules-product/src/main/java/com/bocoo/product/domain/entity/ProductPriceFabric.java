package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 价格设置面料清单 pc_price_fabric
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_price_fabric")
@Schema(description = "价格设置面料清单")
public class ProductPriceFabric extends BaseEntity {

    @TableId(value = "price_fabric_id")
    private Long priceFabricId;
    private Long tenantId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String unitCode;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
