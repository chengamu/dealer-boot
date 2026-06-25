package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 厂家主数据表 pc_manufacturer
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_manufacturer")
@Schema(description = "厂家主数据表")
public class ProductManufacturer extends BaseEntity {

    @TableId(value = "manufacturer_id")
    @Schema(description = "厂家ID")
    private Long manufacturerId;

    @Schema(description = "厂家编号")
    private String manufacturerCode;

    @Schema(description = "厂家名称")
    private String manufacturerName;

    @Schema(description = "厂家简称")
    private String manufacturerShortName;

    @Schema(description = "是否厂家")
    private Boolean manufacturerFlag;

    @Schema(description = "是否供应商")
    private Boolean supplierFlag;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "删除标志")
    private String delFlag;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}
