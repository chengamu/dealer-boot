package com.bocoo.product.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.product.domain.entity.PricePlanVersion;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格方案版本业务对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = PricePlanVersion.class, reverseConvertGenerate = false)
@Schema(description = "价格方案版本业务对象")
public class PricePlanVersionBo extends BaseBo {

    /**
     * 价格方案版本ID
     */
    @Schema(description = "价格方案版本ID")
    private Long pricePlanVersionId;

    /**
     * 价格方案ID
     */
    @Schema(description = "价格方案ID")
    private Long pricePlanId;

    /**
     * 价格方案编码
     */
    @Schema(description = "价格方案编码")
    private String pricePlanCode;

    /**
     * 版本号
     */
    @Schema(description = "版本号")
    private String versionNo;

    /**
     * 版本名称
     */
    @Schema(description = "版本名称")
    private String versionName;

    /**
     * 版本状态：DRAFT草稿，SUBMITTED已提交，PUBLISHED已发布，ARCHIVED已归档
     */
    @Schema(description = "版本状态：DRAFT草稿，SUBMITTED已提交，PUBLISHED已发布，ARCHIVED已归档")
    private String versionStatus;

    /**
     * 产品模型ID
     */
    @Schema(description = "产品模型ID")
    private Long productModelId;

    /**
     * 产品模型编码
     */
    @Schema(description = "产品模型编码")
    private String productModelCode;

    /**
     * 销售变体ID
     */
    @Schema(description = "销售变体ID")
    private Long salesVariantId;

    /**
     * 销售变体编码
     */
    @Schema(description = "销售变体编码")
    private String salesVariantCode;

    /**
     * 币种编码
     */
    @Schema(description = "币种编码")
    private String currencyCode;

    /**
     * 计价模式
     */
    @Schema(description = "计价模式")
    private String pricingMode;

    /**
     * 基础金额
     */
    @Schema(description = "基础金额")
    private BigDecimal baseAmount;

    /**
     * 价格结构快照JSON
     */
    @Schema(description = "价格结构快照JSON")
    private String priceSchemaJson;

    /**
     * 草稿内容哈希
     */
    @Schema(description = "草稿内容哈希")
    private String draftHash;

    /**
     * 生效开始时间，UTC语义
     */
    @Schema(description = "生效开始时间，UTC语义")
    private LocalDateTime effectiveFrom;

    /**
     * 生效结束时间，UTC语义
     */
    @Schema(description = "生效结束时间，UTC语义")
    private LocalDateTime effectiveTo;

    /**
     * 发布后生成的产品发布包ID
     */
    @Schema(description = "发布后生成的产品发布包ID")
    private Long publishedPackageId;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;

}
