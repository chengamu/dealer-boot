package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductCategory;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品分类业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductCategory.class, reverseConvertGenerate = false)
@Schema(description = "产品分类业务对象")
public class ProductCategoryBo extends BaseBo {

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

}
