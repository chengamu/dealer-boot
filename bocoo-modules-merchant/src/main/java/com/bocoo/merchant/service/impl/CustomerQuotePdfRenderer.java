package com.bocoo.merchant.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.merchant.domain.vo.CustomerQuoteItemVo;
import com.bocoo.merchant.domain.vo.CustomerQuoteVo;
import com.lowagie.text.*;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;

@Component
class CustomerQuotePdfRenderer {
    private static final BaseFont PDF_FONT = fontBase();

    byte[] render(CustomerQuoteVo quote) {
        boolean chinese = "ZH_CN".equals(quote.getQuoteLanguage());
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(document, output); document.open();
            document.add(new Paragraph(chinese ? "客户报价单" : "QUOTATION", font(18, Font.BOLD)));
            document.add(new Paragraph((chinese ? "报价编号：" : "Quote No: ") + text(quote.getQuoteNo())
                + "    " + (chinese ? "客户：" : "Customer: ") + text(quote.getCustomerName())
                + "    " + (chinese ? "项目：" : "Project: ") + text(quote.getProjectName()), font(11, Font.NORMAL)));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(9); table.setWidthPercentage(100);
            headers(table, chinese);
            for (CustomerQuoteItemVo item : quote.getItems()) addItem(table, item, chinese);
            document.add(table);
            document.add(new Paragraph((chinese ? "产品金额：" : "Products: ") + money(quote.getProductAmount())
                + "   " + (chinese ? "邮费：" : "Shipping: ") + money(quote.getShippingAmount())
                + "   " + (chinese ? "合计：" : "Total: ") + money(quote.getTotalAmount()), font(11, Font.BOLD)));
            document.close(); return output.toByteArray();
        } catch (Exception e) {
            throw ServiceException.ofMessageKey("customer.quote.pdf.failed");
        }
    }

    private void headers(PdfPTable table, boolean cn) {
        String[] values = cn
            ? new String[]{"序号", "房间", "产品", "宽度(in)", "高度(in)", "数量", "配置", "单价", "金额"}
            : new String[]{"#", "Room", "Product", "Width(in)", "Height(in)", "Qty", "Configuration", "Unit", "Amount"};
        for (String value : values) header(table, value);
    }

    private void addItem(PdfPTable table, CustomerQuoteItemVo item, boolean cn) {
        cell(table, String.valueOf(item.getLineNo())); cell(table, item.getRoomLocation());
        cell(table, item.getSaleProductName()); cell(table, number(item.getOrderWidthInch()));
        cell(table, number(item.getOrderHeightInch())); cell(table, String.valueOf(item.getQuantity()));
        cell(table, cn ? item.getSelectedOptionsSummaryCn() : item.getSelectedOptionsSummaryEn());
        cell(table, money(item.getUnitAmount())); cell(table, money(item.getLineAmount()));
    }

    private void header(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(value, font(10, Font.BOLD)));
        cell.setBackgroundColor(new java.awt.Color(235, 240, 246)); table.addCell(cell);
    }

    private void cell(PdfPTable table, String value) {
        table.addCell(new PdfPCell(new Phrase(text(value), font(9, Font.NORMAL))));
    }

    private Font font(float size, int style) { return new Font(PDF_FONT, size, style); }
    private String text(String value) { return value == null || value.isBlank() ? "-" : value; }
    private String money(BigDecimal value) { return "$" + (value == null ? "0.00" : value.setScale(2)); }
    private String number(BigDecimal value) { return value == null ? "-" : value.stripTrailingZeros().toPlainString(); }
    private static BaseFont fontBase() {
        try { return BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED); }
        catch (Exception e) { throw new IllegalStateException(e); }
    }
}
