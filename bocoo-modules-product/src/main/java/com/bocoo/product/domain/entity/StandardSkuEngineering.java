package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_standard_sku_engineering")
@Schema(description = "标品固定工程配置")
public class StandardSkuEngineering extends BaseEntity {
    @TableId(value = "sku_engineering_id")
    private Long skuEngineeringId;
    private Long tenantId;
    private Long versionId;
    private Long standardSkuId;
    private String standardSkuCode;
    private String standardSkuNameCn;
    private String standardSkuNameEn;
    private String fixedItemsJson;
    private String status;
    private String delFlag;
    private String remark;
}
