package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.utils.StringUtils;
import com.bocoo.common.mail.utils.MailUtils;
import com.bocoo.common.satoken.utils.LoginHelper;
import com.bocoo.dealer.domain.bo.SalesDocumentEmailBo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.bocoo.dealer.domain.entity.SalesDocumentItem;
import com.bocoo.dealer.mapper.SalesDocumentItemMapper;
import com.bocoo.dealer.service.SalesDocumentArtifactService;
import com.bocoo.dealer.service.SalesDocumentQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.nio.file.Files;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SalesDocumentArtifactServiceImpl implements SalesDocumentArtifactService {
    private final SalesDocumentQueryService queryService;
    private final SalesDocumentPdfRenderer renderer;
    private final SalesDocumentEventRecorder events;
    private final SalesDocumentItemMapper itemMapper;
    private final SalesDocumentJson json;
    private final SalesDocumentExportAssembler exportAssembler;

    @Override
    public byte[] pdf(Long id, String type) {
        SalesDocumentVo row = queryService.queryById(id);
        requireProductionAccess(type);
        byte[] bytes = "PRODUCTION".equalsIgnoreCase(type) ? productionPdf(row) : renderer.render(row, type);
        events.record(id, row.getTenantId(), "PDF_GENERATED", null, type, null);
        return bytes;
    }

    @Override
    public String sendEmail(Long id, SalesDocumentEmailBo bo) {
        SalesDocumentVo row = queryService.queryById(id);
        String type = StringUtils.blankToDefault(bo.getDocumentType(), "ORDER").toUpperCase(Locale.ROOT);
        requireProductionAccess(type);
        byte[] bytes = "PRODUCTION".equals(type) ? productionPdf(row) : renderer.render(row, type);
        java.io.File file = null;
        try {
            file = java.io.File.createTempFile("sales-" + id + "-", ".pdf");
            Files.write(file.toPath(), bytes);
            String subject = StringUtils.blankToDefault(bo.getSubject(), type + " " + row.getOrderNo());
            String message = HtmlUtils.htmlEscape(StringUtils.blankToDefault(
                bo.getMessage(), "Please find the attached document.")).replace("\n", "<br>");
            String messageId = MailUtils.sendHtml(bo.getRecipient(), subject, "<p>" + message + "</p>", file);
            events.record(id, row.getTenantId(), "EMAIL_SENT", null, bo.getRecipient(), messageId);
            return messageId;
        } catch (RuntimeException | java.io.IOException e) {
            events.record(id, row.getTenantId(), "EMAIL_FAILED", null, bo.getRecipient(), e.getMessage());
            throw new com.bocoo.common.core.exception.ServiceException(e.getMessage());
        } finally {
            if (file != null) try { Files.deleteIfExists(file.toPath()); } catch (java.io.IOException ignored) { }
        }
    }

    private void requireProductionAccess(String type) {
        if ("PRODUCTION".equalsIgnoreCase(type) && !LoginHelper.isPlatformTenant()) {
            throw com.bocoo.common.core.exception.ServiceException.ofMessageKey("dealer.sales.platformOnly");
        }
    }

    @Override
    public java.util.List<com.bocoo.dealer.domain.vo.SalesDocumentExportVo> export(Long id) {
        SalesDocumentVo row = queryService.queryById(id);
        events.record(id, row.getTenantId(), "EXCEL_EXPORTED", null, "ORDER", null);
        return exportAssembler.rows(row);
    }

    private byte[] productionPdf(SalesDocumentVo row) {
        java.util.List<SalesDocumentItem> items = com.bocoo.common.core.context.TenantContextHolder.callWithIgnore(() ->
            itemMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<SalesDocumentItem>()
                .eq("sales_document_id", row.getSalesDocumentId()).eq("tenant_id", row.getTenantId()).eq("del_flag", "0")
                .orderByAsc("line_no")));
        java.util.List<ProductionMaterialRow> materials = items.stream().flatMap(item -> json.bomItems(item.getBomSnapshotJson()).stream()
            .map(bom -> new ProductionMaterialRow(item.getRoomLocation(), item.getSaleProductName(), bom.getMaterialCode(),
                bom.getMaterialNameCn(), bom.getAttributeGroupNameCn(), bom.getUnitCode(), bom.getUsageQty(), item.getRemark())))
            .toList();
        return renderer.renderProduction(row, materials);
    }
}
