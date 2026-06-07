package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.SalesVariant;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品销售变体视图对象
 */
@Data
@AutoMapper(target = SalesVariant.class)
@Schema(description = "产品销售变体视图对象")
public class SalesVariantVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品销售变体ID
     */
    @Schema(description = "产品销售变体ID")
    private Long variantId;

    /**
     * 产品模型ID
     */
    @Schema(description = "产品模型ID")
    private Long modelId;

    /**
     * 销售变体编码
     */
    @Schema(description = "销售变体编码")
    private String variantCode;

    /**
     * 销售变体中文名称
     */
    @Schema(description = "销售变体中文名称")
    private String variantNameCn;

    /**
     * 销售变体英文名称
     */
    @Schema(description = "销售变体英文名称")
    private String variantNameEn;

    /**
     * 销售维度JSON
     */
    @Schema(description = "销售维度JSON")
    private String dimensionJson;

    /**
     * 市场编码
     */
    @Schema(description = "市场编码")
    private String marketCode;

    /**
     * 控制方式
     */
    @Schema(description = "控制方式")
    private String controlMethod;

    /**
     * 等级
     */
    @Schema(description = "等级")
    private String grade;

    /**
     * 包装类型
     */
    @Schema(description = "包装类型")
    private String packageType;

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
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
