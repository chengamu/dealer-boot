package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductManufacturer;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 厂家业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductManufacturer.class, reverseConvertGenerate = false)
@Schema(description = "厂家业务对象")
public class ProductManufacturerBo extends BaseBo {

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
