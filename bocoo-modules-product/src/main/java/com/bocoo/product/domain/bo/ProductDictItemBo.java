package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductDictItem;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品业务字典项业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductDictItem.class, reverseConvertGenerate = false)
@Schema(description = "产品业务字典项业务对象")
public class ProductDictItemBo extends BaseBo {

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
}
