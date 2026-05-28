package com.bocoo.system.domain.vo;

import com.bocoo.system.domain.entity.SysLegalDocument;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AutoMapper(target = SysLegalDocument.class)
public class SysLegalDocumentVo implements Serializable {

    private Long documentId;

    private String documentType;

    private String locale;

    private String title;

    private String content;

    private String version;

    private String status;

    private LocalDateTime publishedTime;

    private String remark;
}
