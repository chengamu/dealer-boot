package com.bocoo.product.domain.vo;

import com.bocoo.product.domain.entity.ProductComponent;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 产品组件视图对象
 */
@Data
@AutoMapper(target = ProductComponent.class)
@Schema(description = "产品组件视图对象")
public class ProductComponentVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 产品组件ID
     */
    @Schema(description = "产品组件ID")
    private Long componentId;

    /**
     * 组件编码
     */
    @Schema(description = "组件编码")
    private String componentCode;

    /**
     * 组件中文名称
     */
    @Schema(description = "组件中文名称")
    private String componentNameCn;

    /**
     * 组件英文名称
     */
    @Schema(description = "组件英文名称")
    private String componentNameEn;

    /**
     * 组件类型
     */
    @Schema(description = "组件类型")
    private String componentType;

    /**
     * 业务口径类型
     */
    @Schema(description = "业务口径类型")
    private String businessType;

    /**
     * 默认数量
     */
    @Schema(description = "默认数量")
    private BigDecimal defaultQty;

    /**
     * 数量模式
     */
    @Schema(description = "数量模式")
    private String qtyMode;

    /**
     * 单位编码
     */
    @Schema(description = "单位编码")
    private String unitCode;

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
     * 适用范围JSON
     */
    @Schema(description = "适用范围JSON")
    private String scopeJson;

    /**
     * 老系统来源
     */
    @Schema(description = "老系统来源")
    private String legacySource;

    /**
     * 老系统编号
     */
    @Schema(description = "老系统编号")
    private String legacyId;

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
