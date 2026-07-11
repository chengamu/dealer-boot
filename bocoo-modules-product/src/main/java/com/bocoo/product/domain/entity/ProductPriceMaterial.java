package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配方版本物料价格主行。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_price_material")
@Schema(description = "价格设置物料清单")
public class ProductPriceMaterial extends BaseEntity {

    @TableId(value = "price_material_id")
    private Long priceMaterialId;
    private Long tenantId;
    private Long priceSettingId;
    private Long saleProductId;
    private Long formulaVersionId;
    private Long formulaMaterialId;
    private Long materialId;
    private String materialCode;
    private String materialNameCn;
    private String specModelText;
    private String attributeGroupCode;
    private String attributeGroupNameCn;
    private String materialTypeCode;
    private String materialTypeNameCn;
    private String unitCode;
    private String status;
    private Integer sortOrder;
    private String delFlag;
    private String remark;
}
