package com.bocoo.demo.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseEntity;
import com.bocoo.demo.domain.entity.Sample;
import io.github.linpeilie.annotations.AutoMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 样本业务对象 sample_record
 *
 * @author validator
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = Sample.class, reverseConvertGenerate = false)
@Schema(description = "样本业务对象")
public class SampleBo extends BaseEntity {

    /**
     * 样本ID
     */
    @Schema(description = "样本ID")
    private Long id;

    /**
     * 样本名称
     */
    @Schema(description = "样本名称")
    private String sampleName;

    /**
     * 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

}
