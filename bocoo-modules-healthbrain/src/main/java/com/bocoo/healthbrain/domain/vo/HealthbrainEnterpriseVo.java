package com.bocoo.healthbrain.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.healthbrain.domain.entity.HealthbrainEnterprise;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 企业管理视图对象 healthbrain_enterprise
 *
 * @author cmx
 */
@Data
@ExcelIgnoreUnannotated
@AutoMapper(target = HealthbrainEnterprise.class)
@Schema(description = "企业管理视图对象")
public class HealthbrainEnterpriseVo {

    /**
     * 
     */
    @ExcelProperty(value = "")
    @Schema(description = "")
    private Long id;

    /**
     * 企业类型
     */
    @ExcelProperty(value = "企业类型")
    @Schema(description = "企业类型")
    private String type;

    /**
     * 企业名称
     */
    @ExcelProperty(value = "企业名称")
    @Schema(description = "企业名称")
    private String name;

    /**
     * 企业Logo URL
     */
    @ExcelProperty(value = "企业Logo URL")
    @Schema(description = "企业Logo URL")
    private String logo;

    /**
     * 发展历程
     */
    @ExcelProperty(value = "发展历程")
    @Schema(description = "发展历程")
    private String development;

    /**
     * 企业地址
     */
    @ExcelProperty(value = "企业地址")
    @Schema(description = "企业地址")
    private String address;

    /**
     * 主营业务
     */
    @ExcelProperty(value = "主营业务")
    @Schema(description = "主营业务")
    private String mainBusiness;

    /**
     * 企业描述
     */
    @ExcelProperty(value = "企业描述")
    @Schema(description = "企业描述")
    private String description;

    /**
     * 联系人
     */
    @ExcelProperty(value = "联系人")
    @Schema(description = "联系人")
    private String contact;

    /**
     * 联系方式
     */
    @ExcelProperty(value = "联系方式")
    @Schema(description = "联系方式")
    private String contactInfo;

    /**
     * 对接信息
     */
    @ExcelProperty(value = "对接信息")
    @Schema(description = "对接信息")
    private String dock;

    
    @ExcelProperty(value = "创建人")
    @Schema(description = "创建人")
    private String createBy;


    /**
     * 是否重点
     */
    @Schema(description = "是否重点")
    private String important;


    @Schema(description = "企业类型名称")
    private String typeName;

    @Schema(description = "发展历程名称")
    private String developmentName;

    @Schema(description = "是否银发经济")
    private String silverEconomy;

    @Schema(description = "企业地点")
    private String location;

    @Schema(description = "是否生命健康类")
    private String lifeHealth;

    @Schema(description = "企业类型")
    private String enterpriseCategory;

    @Schema(description = "项目进展")
    private String projectProgress;


    /**
     * 创建时间
     */
    @ExcelProperty(value = "创建时间")
    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
