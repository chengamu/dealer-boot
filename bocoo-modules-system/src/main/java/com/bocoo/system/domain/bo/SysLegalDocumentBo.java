package com.bocoo.system.domain.bo;

import com.bocoo.common.mybatis.core.domain.BaseBo;
import com.bocoo.system.domain.entity.SysLegalDocument;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = SysLegalDocument.class, reverseConvertGenerate = false)
public class SysLegalDocumentBo extends BaseBo {

    private Long documentId;

    @NotBlank(message = "{legal.document.type.required}")
    @Size(max = 30, message = "{legal.document.type.max}")
    private String documentType;

    @NotBlank(message = "{legal.document.locale.required}")
    @Size(max = 20, message = "{legal.document.locale.max}")
    private String locale;

    @NotBlank(message = "{legal.document.title.required}")
    @Size(max = 200, message = "{legal.document.title.max}")
    private String title;

    @NotBlank(message = "{legal.document.content.required}")
    private String content;

    @Size(max = 50, message = "{legal.document.version.max}")
    private String version;

    private String status;

    private LocalDateTime publishedTime;
}
