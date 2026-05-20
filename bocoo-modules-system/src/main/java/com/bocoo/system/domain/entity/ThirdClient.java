package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 第三方应用实体表 third_client
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("third_client")
@Schema(description = "第三方应用表")
public class ThirdClient extends BaseEntity {



    @Schema(description = "主键ID")
    @TableId(value = "id")
    private Long id;

    @Schema(description = "应用唯一标识")
    private String appKey;

    @Schema(description = "应用密钥")
    private String secretKey;

    @Schema(description = "应用名称")
    private String name;

    @Schema(description = "权限列表")
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> permissions;



    @Schema(description = "删除标志（0存在 1删除）")
    @TableLogic
    private String delFlag;
}
