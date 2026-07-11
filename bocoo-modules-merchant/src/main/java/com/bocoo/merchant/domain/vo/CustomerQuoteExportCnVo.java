package com.bocoo.merchant.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerQuoteExportCnVo {
    @ExcelProperty("报价单号")
    private String quoteNo;
    @ExcelProperty("客户")
    private String customerName;
    @ExcelProperty("公司")
    private String companyName;
    @ExcelProperty("项目")
    private String projectName;
    @ExcelProperty("有效期")
    private LocalDate validUntil;
    @ExcelProperty("房间/位置")
    private String roomLocation;
    @ExcelProperty("产品")
    private String saleProductName;
    @ExcelProperty("宽度(in)")
    private BigDecimal widthInch;
    @ExcelProperty("高度(in)")
    private BigDecimal heightInch;
    @ExcelProperty("数量")
    private Integer quantity;
    @ExcelProperty("配置")
    private String configuration;
    @ExcelProperty("单价")
    private BigDecimal unitAmount;
    @ExcelProperty("邮费")
    private BigDecimal shippingAmount;
    @ExcelProperty("行金额")
    private BigDecimal lineAmount;
    @ExcelProperty("报价合计")
    private BigDecimal quoteTotal;
}
