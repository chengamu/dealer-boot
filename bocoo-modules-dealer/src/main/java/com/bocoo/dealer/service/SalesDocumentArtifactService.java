package com.bocoo.dealer.service;

import com.bocoo.dealer.domain.bo.SalesDocumentEmailBo;
import com.bocoo.dealer.domain.vo.SalesDocumentExportVo;

import java.util.List;

public interface SalesDocumentArtifactService {
    byte[] pdf(Long id, String type);
    String sendEmail(Long id, SalesDocumentEmailBo bo);
    List<SalesDocumentExportVo> export(Long id);
}
