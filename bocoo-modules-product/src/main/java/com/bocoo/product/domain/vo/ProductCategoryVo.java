package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductCategory;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品分类视图对象
 */
@Data
@AutoMapper(target = ProductCategory.class)
@Schema(description = "产品分类视图对象")
public class ProductCategoryVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品分类ID
     */
    @Schema(description = "产品分类ID")
    private Long categoryId;

    /**
     * 父分类ID
     */
    @Schema(description = "父分类ID")
    private Long parentId;

    /**
     * 产品分类编码
     */
    @Schema(description = "产品分类编码")
    private String categoryCode;

    /**
     * 产品分类中文名称
     */
    @Schema(description = "产品分类中文名称")
    private String categoryNameCn;

    /**
     * 产品分类英文名称
     */
    @Schema(description = "产品分类英文名称")
    private String categoryNameEn;

    /**
     * 分类层级
     */
    @Schema(description = "分类层级")
    private Integer categoryLevel;

    /**
     * 分类路径编码
     */
    @Schema(description = "分类路径编码")
    private String categoryPath;

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
