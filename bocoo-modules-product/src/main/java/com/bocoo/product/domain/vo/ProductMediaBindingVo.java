package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductMediaBinding;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品资料绑定视图对象
 */
@Data
@AutoMapper(target = ProductMediaBinding.class)
@Schema(description = "产品资料绑定视图对象")
public class ProductMediaBindingVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

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
