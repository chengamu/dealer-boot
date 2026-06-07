package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.QuestionGroup;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 配置问题组业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = QuestionGroup.class, reverseConvertGenerate = false)
@Schema(description = "配置问题组业务对象")
public class QuestionGroupBo extends BaseBo {

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

}
