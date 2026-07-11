package com.bocoo.dealer.service.impl;

import com.bocoo.common.core.exception.ServiceException;
import com.bocoo.dealer.domain.vo.SalesDocumentItemVo;
import com.bocoo.dealer.domain.vo.SalesDocumentVo;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Locale;

@Component
class SalesDocumentPdfRenderer {
    private static final BaseFont PDF_FONT = createBaseFont();

    byte[] render(SalesDocumentVo row, String requestedType) {
        String type = requestedType == null ? "ORDER" : requestedType.toUpperCase(Locale.ROOT);
        if (!java.util.Set.of("ORDER", "PRODUCTION").contains(type)) {
            throw ServiceException.ofMessageKey("dealer.sales.pdfTypeInvalid");
        }
        if (row.getOrderNo() == null) {
            throw ServiceException.ofMessageKey("dealer.sales.orderRequired");
        }
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(document, output);
            document.open();
            document.add(new Paragraph(title(type), font(18, Font.BOLD)));
            document.add(new Paragraph("No: " + number(row, type) + "    Customer: " + text(row.getCustomerName())
                + "    Project: " + text(row.getProjectName()), font(11, Font.NORMAL)));
            document.add(new Paragraph("Merchant: " + text(row.getMerchantName()) + "    Owner: " + text(row.getOwnerName())
                + "    Ship to: " + text(row.getShippingAddress()), font(11, Font.NORMAL)));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable("PRODUCTION".equals(type) ? 8 : 9);
            table.setWidthPercentage(100);
            headers(table, type);
            for (SalesDocumentItemVo item : row.getItems()) addItem(table, item, type);
            document.add(table);
            if (!"PRODUCTION".equals(type)) {
                document.add(new Paragraph("List: " + money(row.getListAmount()) + "   Discount: " + money(row.getDiscountAmount())
                    + "   Shipping: " + money(row.getShippingAmount()) + "   Tax: " + money(row.getTaxAmount())
                    + "   Total: " + money(row.getTotalAmount()), font(11, Font.BOLD)));
            }
            document.close();
            return output.toByteArray();
        } catch (Exception e) {
            throw ServiceException.ofMessageKey("dealer.sales.pdfFailed");
        }
    }

    byte[] renderProduction(SalesDocumentVo row, java.util.List<ProductionMaterialRow> materials) {
        if (row.getOrderNo() == null) throw ServiceException.ofMessageKey("dealer.sales.orderRequired");
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 28, 28, 28, 28);
            PdfWriter.getInstance(document, output); document.open();
            document.add(new Paragraph("PRODUCTION SHEET", font(18, Font.BOLD)));
            document.add(new Paragraph("Order: " + row.getOrderNo() + "    Merchant: " + text(row.getMerchantName())
                + "    Customer: " + text(row.getCustomerName()), font(11, Font.NORMAL)));
            document.add(Chunk.NEWLINE);
            PdfPTable table = new PdfPTable(8); table.setWidthPercentage(100);
            for (String value : new String[]{"Room", "Product", "Material Code", "Material", "Group", "Unit", "Usage", "Remark"}) {
                headerCell(table, value);
            }
            for (ProductionMaterialRow material : materials) {
                cell(table, material.room()); cell(table, material.product());
                cell(table, material.materialCode()); cell(table, material.materialName());
                cell(table, material.groupName()); cell(table, material.unitCode());
                cell(table, material.usageQty() == null ? "-" : material.usageQty().stripTrailingZeros().toPlainString());
                cell(table, material.remark());
            }
            document.add(table); document.close(); return output.toByteArray();
        } catch (Exception e) {
            throw ServiceException.ofMessageKey("dealer.sales.pdfFailed");
        }
    }

    private void headers(PdfPTable table, String type) {
        String[] values = "PRODUCTION".equals(type)
            ? new String[]{"#", "Room", "Product", "Size", "Qty", "Configuration", "Formula", "Remark"}
            : new String[]{"#", "Room", "Product", "Width", "Height", "Qty", "Configuration", "Unit", "Amount"};
        for (String value : values) headerCell(table, value);
    }

    private void addItem(PdfPTable table, SalesDocumentItemVo item, String type) {
        cell(table, String.valueOf(item.getLineNo())); cell(table, item.getRoomLocation());
        cell(table, item.getSaleProductName());
        if ("PRODUCTION".equals(type)) {
            cell(table, item.getOrderWidthInch() + " x " + item.getOrderHeightInch() + " in");
            cell(table, String.valueOf(item.getQuantity())); cell(table, item.getConfigurationSummary());
            cell(table, item.getFormulaVersionLabel()); cell(table, item.getRemark());
        } else {
            cell(table, String.valueOf(item.getOrderWidthInch())); cell(table, String.valueOf(item.getOrderHeightInch()));
            cell(table, String.valueOf(item.getQuantity())); cell(table, item.getConfigurationSummary());
            cell(table, money(item.getUnitAmount())); cell(table, money(item.getLineAmount()));
        }
    }

    private void headerCell(PdfPTable table, String value) {
        PdfPCell cell = new PdfPCell(new Phrase(text(value), font(10, Font.BOLD)));
        cell.setBackgroundColor(new java.awt.Color(235, 240, 246)); table.addCell(cell);
    }

    private void cell(PdfPTable table, String value) {
        table.addCell(new PdfPCell(new Phrase(text(value), font(9, Font.NORMAL))));
    }

    private Font font(float size, int style) { return new Font(PDF_FONT, size, style); }
    private static BaseFont createBaseFont() {
        try { return BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED); }
        catch (Exception e) { throw new IllegalStateException("Unable to initialize PDF font", e); }
    }

    private String title(String type) { return "PRODUCTION".equals(type) ? "PRODUCTION SHEET" : "SALES ORDER"; }
    private String number(SalesDocumentVo row, String type) { return row.getOrderNo(); }
    private String text(String value) { return value == null || value.isBlank() ? "-" : value; }
    private String money(BigDecimal value) { return "$" + (value == null ? "0.00" : value.setScale(2)); }
}
