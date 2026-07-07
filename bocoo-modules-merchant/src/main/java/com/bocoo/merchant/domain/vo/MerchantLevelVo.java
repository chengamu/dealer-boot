package com.bocoo.merchant.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.merchant.domain.entity.MerchantLevel;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = MerchantLevel.class)
public class MerchantLevelVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long levelId;
    private Long tenantId;
    @ExcelProperty("等级编码")
    private String levelCode;
    @ExcelProperty("等级名称")
    private String levelName;
    @ExcelProperty("默认折扣")
    private BigDecimal defaultDiscountRate;
    @ExcelProperty("默认信用额度")
    private BigDecimal defaultCreditLimit;
    private Boolean defaultFlag;
    private Integer sortOrder;
    @ExcelProperty("状态")
    private String status;
    private String delFlag;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
