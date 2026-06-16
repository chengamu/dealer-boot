package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductDictItem;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品业务字典项视图对象
 */
@Data
@AutoMapper(target = ProductDictItem.class)
@Schema(description = "产品业务字典项视图对象")
public class ProductDictItemVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long dictItemId;
    private Long tenantId;
    private String dictTypeCode;
    private String dictItemValue;
    private String dictItemLabelCn;
    private String dictItemLabelEn;
    private String parentValue;
    private Boolean systemFlag;
    private Boolean editableFlag;
    private String status;
    private Integer sortOrder;
    private String remark;
    private String delFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
