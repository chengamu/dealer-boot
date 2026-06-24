package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductChangeLog;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品业务变更流水查询对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductChangeLog.class, reverseConvertGenerate = false)
@Schema(description = "产品业务变更流水查询对象")
public class ProductChangeLogBo extends BaseBo {

    private Long changeLogId;
    private Long tenantId;
    private String bizModule;
    private String bizType;
    private Long bizId;
    private String bizCode;
    private String actionType;
    private String operatorName;
}
