package com.bocoo.merchant.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CustomerQuoteExportEnVo {
    @ExcelProperty("Quote No.")
    private String quoteNo;
    @ExcelProperty("Customer")
    private String customerName;
    @ExcelProperty("Company")
    private String companyName;
    @ExcelProperty("Project")
    private String projectName;
    @ExcelProperty("Valid Until")
    private LocalDate validUntil;
    @ExcelProperty("Room / Location")
    private String roomLocation;
    @ExcelProperty("Product")
    private String saleProductName;
    @ExcelProperty("Width (in)")
    @NumberFormat("# ??/??") private BigDecimal widthInch;
    @ExcelProperty("Height (in)")
    @NumberFormat("# ??/??") private BigDecimal heightInch;
    @ExcelProperty("Qty")
    private Integer quantity;
    @ExcelProperty("Configuration")
    private String configuration;
    @ExcelProperty("Unit Price")
    @NumberFormat("0.00") private BigDecimal unitAmount;
    @ExcelProperty("Shipping")
    @NumberFormat("0.00") private BigDecimal shippingAmount;
    @ExcelProperty("Line Total")
    @NumberFormat("0.00") private BigDecimal lineAmount;
    @ExcelProperty("Quote Total")
    @NumberFormat("0.00") private BigDecimal quoteTotal;
}
