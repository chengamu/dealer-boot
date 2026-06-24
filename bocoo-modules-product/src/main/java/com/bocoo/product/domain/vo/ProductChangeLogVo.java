package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductChangeLog;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品业务变更流水视图对象
 */
@Data
@AutoMapper(target = ProductChangeLog.class)
@Schema(description = "产品业务变更流水视图对象")
public class ProductChangeLogVo implements Serializable {

    private Long changeLogId;
    private Long tenantId;
    private String bizModule;
    private String bizType;
    private Long bizId;
    private String bizCode;
    private String actionType;
    private String actionName;
    private String beforeJson;
    private String afterJson;
    private String diffJson;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime operateTime;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
}
