package com.bocoo.merchant.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.merchant.domain.entity.MerchantLevelDiscount;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MerchantLevelDiscount.class)
public class MerchantLevelDiscountVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long discountId;
    private Long tenantId;
    private Long levelId;
    @ExcelProperty("等级编码")
    private String levelCode;
    @ExcelProperty("等级名称")
    private String levelName;
    private Long categoryId;
    @ExcelProperty("产品分类")
    private String categoryNameCn;
    @ExcelProperty("产品类型")
    private String productTypeNameCn;
    @ExcelProperty("折扣")
    private BigDecimal discountRate;
    private Integer sortOrder;
    @ExcelProperty("状态")
    private String status;
    private String delFlag;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
