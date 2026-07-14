package com.bocoo.dealer.quickorder.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PlatformQuickOrderExportVo {
    @ExcelProperty("Quick Order No.") private String quickOrderNo;
    @ExcelProperty("Business Origin") private String businessOrigin;
    @ExcelProperty("Tenant ID") private Long tenantId;
    @ExcelProperty("Sales Store ID") private Long salesStoreId;
    @ExcelProperty("Customer") private String customerName;
    @ExcelProperty("Owner") private String ownerName;
    @ExcelProperty("Status") private String status;
    @ExcelProperty("Product Types") private Integer itemTypeCount;
    @ExcelProperty("Total Quantity") private Integer totalQuantity;
    @ExcelProperty("Total Amount") @NumberFormat("0.00") private BigDecimal totalAmount;
    @ExcelProperty("Order No.") private String orderNo;
    @ExcelProperty("Last Updated") private LocalDateTime updateTime;
}
