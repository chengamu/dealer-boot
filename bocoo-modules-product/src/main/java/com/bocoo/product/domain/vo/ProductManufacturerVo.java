package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductManufacturer;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 厂家视图对象
 */
@Data
@AutoMapper(target = ProductManufacturer.class)
@Schema(description = "厂家视图对象")
public class ProductManufacturerVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
