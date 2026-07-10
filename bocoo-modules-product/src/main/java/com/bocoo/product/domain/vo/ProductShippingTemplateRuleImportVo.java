package com.bocoo.product.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class ProductShippingTemplateRuleImportVo {

    @ExcelProperty("适用条件")
    private String condition;

    @ExcelProperty("最小面积(ft²)")
    private BigDecimal minAreaSqft;

    @ExcelProperty("最大面积(ft²)")
    private BigDecimal maxAreaSqft;

    @ExcelProperty("邮费")
    private BigDecimal feeAmount;
}
