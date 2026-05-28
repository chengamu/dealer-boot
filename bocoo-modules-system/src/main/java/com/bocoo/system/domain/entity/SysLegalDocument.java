package com.bocoo.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.bocoo.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_legal_document")
public class SysLegalDocument extends BaseEntity {

    @TableId(value = "document_id")
    private Long documentId;

    private String documentType;

    private String locale;

    private String title;

    private String content;

    private String version;

    private String status;

    private LocalDateTime publishedTime;
}
