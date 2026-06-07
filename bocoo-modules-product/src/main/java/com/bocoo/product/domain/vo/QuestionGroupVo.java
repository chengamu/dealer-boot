package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.QuestionGroup;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 配置问题组视图对象
 */
@Data
@AutoMapper(target = QuestionGroup.class)
@Schema(description = "配置问题组视图对象")
public class QuestionGroupVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 配置问题组ID
     */
    @Schema(description = "配置问题组ID")
    private Long questionGroupId;

    /**
     * 问题组编码
     */
    @Schema(description = "问题组编码")
    private String groupCode;

    /**
     * 问题组中文名称
     */
    @Schema(description = "问题组中文名称")
    private String groupNameCn;

    /**
     * 问题组英文名称
     */
    @Schema(description = "问题组英文名称")
    private String groupNameEn;

    /**
     * 问题组中文说明
     */
    @Schema(description = "问题组中文说明")
    private String descriptionCn;

    /**
     * 问题组英文说明
     */
    @Schema(description = "问题组英文说明")
    private String descriptionEn;

    /**
     * 状态：1正常，0停用
     */
    @Schema(description = "状态：1正常，0停用")
    private String status;

    /**
     * 删除标志：0存在，2删除
     */
    @Schema(description = "删除标志：0存在，2删除")
    private String delFlag;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sortOrder;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间，UTC语义
     */
    @Schema(description = "创建时间，UTC语义")
    private LocalDateTime createTime;

    /**
     * 更新时间，UTC语义
     */
    @Schema(description = "更新时间，UTC语义")
    private LocalDateTime updateTime;
}
