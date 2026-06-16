package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductDictType;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品业务字典类型业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductDictType.class, reverseConvertGenerate = false)
@Schema(description = "产品业务字典类型业务对象")
public class ProductDictTypeBo extends BaseBo {

    private Long dictTypeId;
    private Long tenantId;
    private String dictTypeCode;
    private String dictTypeNameCn;
    private String dictTypeNameEn;
    private String businessDomain;
    private Boolean systemFlag;
    private Boolean editableFlag;
    private String status;
    private Integer sortOrder;
    private String remark;
    private String delFlag;
}
