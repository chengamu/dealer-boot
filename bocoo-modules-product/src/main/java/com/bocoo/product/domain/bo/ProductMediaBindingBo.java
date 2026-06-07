package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.ProductMediaBinding;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 产品资料绑定业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = ProductMediaBinding.class, reverseConvertGenerate = false)
@Schema(description = "产品资料绑定业务对象")
public class ProductMediaBindingBo extends BaseBo {

    /**
     * 产品资料绑定ID
     */
    @Schema(description = "产品资料绑定ID")
    private Long bindingId;

    /**
     * 资料资产ID
     */
    @Schema(description = "资料资产ID")
    private Long assetId;

    /**
     * 资料资产编码快照
     */
    @Schema(description = "资料资产编码快照")
    private String assetCode;

    /**
     * 绑定对象类型
     */
    @Schema(description = "绑定对象类型")
    private String targetType;

    /**
     * 绑定对象ID
     */
    @Schema(description = "绑定对象ID")
    private Long targetId;

    /**
     * 绑定对象编码快照
     */
    @Schema(description = "绑定对象编码快照")
    private String targetCode;

    /**
     * 资料用途
     */
    @Schema(description = "资料用途")
    private String usageType;

    /**
     * 可见范围
     */
    @Schema(description = "可见范围")
    private String visibility;

    /**
     * 语言编码
     */
    @Schema(description = "语言编码")
    private String languageCode;

    /**
     * 发布是否必需：1是，0否
     */
    @Schema(description = "发布是否必需：1是，0否")
    private String requiredForPublish;

    /**
     * 排序
     */
    @Schema(description = "排序")
    private Integer sortOrder;

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
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
