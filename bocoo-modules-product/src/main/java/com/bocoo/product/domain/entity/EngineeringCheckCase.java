package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_engineering_check_case")
@Schema(description = "工程配置检查样例")
public class EngineeringCheckCase extends BaseEntity {
    @TableId(value = "check_case_id")
    private Long checkCaseId;
    private Long tenantId;
    private Long versionId;
    private String caseCode;
    private String caseNameCn;
    private String caseNameEn;
    private String inputJson;
    private String expectedJson;
    private String status;
    private String delFlag;
    private Integer sortOrder;
    private String remark;
}
