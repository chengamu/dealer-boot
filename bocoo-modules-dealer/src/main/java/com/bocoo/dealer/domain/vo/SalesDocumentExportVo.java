package com.bocoo.dealer.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesDocumentExportVo {
    @ExcelProperty("Document No.") private String documentNo;
    @ExcelProperty("Customer") private String customerName;
    @ExcelProperty("Company") private String companyName;
    @ExcelProperty("Project") private String projectName;
    @ExcelProperty("Room / Location") private String roomLocation;
    @ExcelProperty("Product") private String saleProductName;
    @ExcelProperty("Width (in)") private BigDecimal widthInch;
    @ExcelProperty("Height (in)") private BigDecimal heightInch;
    @ExcelProperty("Qty") private Integer quantity;
    @ExcelProperty("Configuration") private String configuration;
    @ExcelProperty("Unit Price") private BigDecimal unitAmount;
    @ExcelProperty("Shipping") private BigDecimal shippingAmount;
    @ExcelProperty("Line Total") private BigDecimal lineAmount;
    @ExcelProperty("Document Total") private BigDecimal documentTotal;
}
