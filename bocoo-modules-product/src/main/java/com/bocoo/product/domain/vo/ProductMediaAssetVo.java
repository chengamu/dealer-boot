package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductMediaAsset;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品资料资产视图对象
 */
@Data
@AutoMapper(target = ProductMediaAsset.class)
@Schema(description = "产品资料资产视图对象")
public class ProductMediaAssetVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品资料资产ID
     */
    @Schema(description = "产品资料资产ID")
    private Long assetId;

    /**
     * 资料资产编码
     */
    @Schema(description = "资料资产编码")
    private String assetCode;

    /**
     * 资料资产中文名称
     */
    @Schema(description = "资料资产中文名称")
    private String assetNameCn;

    /**
     * 资料资产英文名称
     */
    @Schema(description = "资料资产英文名称")
    private String assetNameEn;

    /**
     * 资料类型
     */
    @Schema(description = "资料类型")
    private String assetType;

    /**
     * 资料用途
     */
    @Schema(description = "资料用途")
    private String usageType;

    /**
     * 语言编码
     */
    @Schema(description = "语言编码")
    private String languageCode;

    /**
     * 可见范围
     */
    @Schema(description = "可见范围")
    private String visibility;

    /**
     * OSS文件ID
     */
    @Schema(description = "OSS文件ID")
    private Long ossId;

    /**
     * 文件访问地址
     */
    @Schema(description = "文件访问地址")
    private String url;

    /**
     * 图片替代文本
     */
    @Schema(description = "图片替代文本")
    private String altText;

    /**
     * 资料版本号
     */
    @Schema(description = "资料版本号")
    private Integer versionNo;

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
