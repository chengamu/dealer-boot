package com.bocoo.demo.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

/**
 * 样本对象 sample_record
 *
 * @author validator
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sample_record")
@Schema(description = "样本对象")
public class Sample extends BaseEntity {

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
