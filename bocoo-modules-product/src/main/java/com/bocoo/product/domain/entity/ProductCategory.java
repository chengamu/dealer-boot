package com.bocoo.product.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品分类表 pc_category
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("pc_category")
@Schema(description = "产品分类表")
public class ProductCategory extends BaseEntity {

    /**
     * 产品分类ID
     */
    @TableId(value = "category_id")
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

}
