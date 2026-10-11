package com.rentatools.infrastructure.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.rentatools.domain.billing.Invoice;
import com.rentatools.domain.billing.InvoiceItem;
import com.rentatools.domain.billing.port.PdfPort;
import com.rentatools.domain.common.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class OpenPdfInvoiceAdapter implements PdfPort {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public byte[] generateInvoicePdf(Invoice invoice) {
        if (invoice == null) {
            throw new BusinessException("No se puede generar PDF para una factura nula");
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Colores corporativos (Industrial Dark + Ámbar)
            Color brandDark = new Color(11, 15, 25);
            Color brandAmber = new Color(245, 158, 11);
            Color brandLightGray = new Color(241, 245, 249);
            Color textMuted = new Color(100, 116, 139);

            // Fuentes
            Font fontHeaderTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.WHITE);
            Font fontHeaderSubtitle = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(226, 232, 240));
            Font fontSectionTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, brandDark);
            Font fontRegular = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font fontTableHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
            Font fontFooter = FontFactory.getFont(FontFactory.HELVETICA, 8, textMuted);

            // 1. BANNER DE ENCABEZADO
            PdfPTable headerBanner = new PdfPTable(2);
            headerBanner.setWidthPercentage(100);
            headerBanner.setWidths(new float[]{60, 40});

            PdfPCell cellLeft = new PdfPCell();
            cellLeft.setBackgroundColor(brandDark);
            cellLeft.setPadding(14);
            cellLeft.setBorder(0);

            Paragraph title = new Paragraph("🏗️ RENTATOOLS PRO", fontHeaderTitle);
            Paragraph subtitle = new Paragraph("Plataforma de Renta de Maquinaria y Equipos Pesados\nNIT: 901.458.789-3 | Bogotá, Colombia\nEmail: facturacion@rentatools.com | Tel: +57 (601) 345-6789", fontHeaderSubtitle);
            cellLeft.addElement(title);
            cellLeft.addElement(subtitle);
            headerBanner.addCell(cellLeft);

            PdfPCell cellRight = new PdfPCell();
            cellRight.setBackgroundColor(brandDark);
            cellRight.setPadding(14);
            cellRight.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellRight.setBorder(0);

            Paragraph invTitle = new Paragraph("FACTURA ELECTRÓNICA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, brandAmber));
            invTitle.setAlignment(Element.ALIGN_RIGHT);
            Paragraph invNum = new Paragraph(invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "INV-DRAFT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE));
            invNum.setAlignment(Element.ALIGN_RIGHT);
            String statusText = invoice.isPaid() ? "ESTADO: PAGADA ✅" : "ESTADO: PENDIENTE DE PAGO ⏳";
            Paragraph statusPara = new Paragraph(statusText, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, invoice.isPaid() ? new Color(34, 197, 94) : brandAmber));
            statusPara.setAlignment(Element.ALIGN_RIGHT);

            cellRight.addElement(invTitle);
            cellRight.addElement(invNum);
            cellRight.addElement(statusPara);
            headerBanner.addCell(cellRight);

            document.add(headerBanner);
            document.add(new Paragraph(" "));

            // 2. METADATOS: CLIENTE Y DETALLES DE RENTA
            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setWidths(new float[]{50, 50});

            PdfPCell clientCard = new PdfPCell();
            clientCard.setBackgroundColor(brandLightGray);
            clientCard.setPadding(10);
            clientCard.setBorderColor(new Color(203, 213, 225));
            clientCard.addElement(new Paragraph("DATOS DEL ARRENDATARIO", fontSectionTitle));
            clientCard.addElement(new Paragraph("Cliente: " + (invoice.getClientName() != null ? invoice.getClientName() : "N/A"), fontBold));
            clientCard.addElement(new Paragraph("Identificación / NIT: " + (invoice.getClientTaxId() != null ? invoice.getClientTaxId() : "N/A"), fontRegular));
            clientCard.addElement(new Paragraph("ID Cliente: #" + (invoice.getClientId() != null ? invoice.getClientId() : "N/A"), fontRegular));
            metaTable.addCell(clientCard);

            PdfPCell rentCard = new PdfPCell();
            rentCard.setBackgroundColor(brandLightGray);
            rentCard.setPadding(10);
            rentCard.setBorderColor(new Color(203, 213, 225));
            rentCard.addElement(new Paragraph("DETALLES DEL CONTRATO DE RENTA", fontSectionTitle));
            rentCard.addElement(new Paragraph("No. Alquiler / Reserva: #" + (invoice.getRentalId() != null ? invoice.getRentalId() : "N/A"), fontBold));
            rentCard.addElement(new Paragraph("Fecha de Emisión: " + (invoice.getIssueDate() != null ? invoice.getIssueDate().format(DATE_FORMATTER) : "N/A"), fontRegular));
            rentCard.addElement(new Paragraph("Fecha de Vencimiento: " + (invoice.getDueDate() != null ? invoice.getDueDate().format(DATE_FORMATTER) : "N/A"), fontRegular));
            if (invoice.isPaid() && invoice.getPaymentDate() != null) {
                rentCard.addElement(new Paragraph("Fecha de Pago: " + invoice.getPaymentDate().format(DATE_FORMATTER), fontBold));
            }
            metaTable.addCell(rentCard);

            document.add(metaTable);
            document.add(new Paragraph(" "));

            // 3. TABLA DE ÍTEMS Y EQUIPOS ARRENDADOS
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{45, 15, 20, 20});

            String[] headers = {"Descripción de la Maquinaria / Equipo", "Días", "Tarifa Diaria", "Subtotal"};
            for (String h : headers) {
                PdfPCell th = new PdfPCell(new Phrase(h, fontTableHeader));
                th.setBackgroundColor(brandDark);
                th.setPadding(8);
                th.setHorizontalAlignment(h.equals("Descripción de la Maquinaria / Equipo") ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT);
                itemsTable.addCell(th);
            }

            if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
                for (InvoiceItem item : invoice.getItems()) {
                    PdfPCell desc = new PdfPCell(new Phrase(item.getToolName() != null ? item.getToolName() : "Herramienta #" + item.getToolId(), fontRegular));
                    desc.setPadding(6);
                    itemsTable.addCell(desc);

                    PdfPCell days = new PdfPCell(new Phrase(String.valueOf(item.getDays()), fontRegular));
                    days.setPadding(6);
                    days.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    itemsTable.addCell(days);

                    PdfPCell rate = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(item.getDailyRate()), fontRegular));
                    rate.setPadding(6);
                    rate.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    itemsTable.addCell(rate);

                    PdfPCell sub = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(item.getSubtotal()), fontRegular));
                    sub.setPadding(6);
                    sub.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    itemsTable.addCell(sub);
                }
            } else {
                PdfPCell empty = new PdfPCell(new Phrase("Sin equipos registrados en este desglose", fontRegular));
                empty.setColspan(4);
                empty.setPadding(8);
                itemsTable.addCell(empty);
            }

            document.add(itemsTable);
            document.add(new Paragraph(" "));

            // 4. TABLA DE TOTALES FISCALES Y GARANTÍA
            PdfPTable totalsTable = new PdfPTable(2);
            totalsTable.setWidthPercentage(45);
            totalsTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.setWidths(new float[]{55, 45});

            addTotalRow(totalsTable, "Subtotal Neto:", CURRENCY_FORMAT.format(invoice.getSubtotal()), fontRegular);
            addTotalRow(totalsTable, "Depósito Garantía (Reembolsable):", CURRENCY_FORMAT.format(invoice.getDepositAmount()), fontRegular);
            addTotalRow(totalsTable, "IVA (19%):", CURRENCY_FORMAT.format(invoice.getTaxAmount()), fontRegular);

            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL A PAGAR:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, brandAmber)));
            totalLabel.setBackgroundColor(brandDark);
            totalLabel.setPadding(8);
            totalsTable.addCell(totalLabel);

            PdfPCell totalVal = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(invoice.getTotalAmount()), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE)));
            totalVal.setBackgroundColor(brandDark);
            totalVal.setPadding(8);
            totalVal.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalsTable.addCell(totalVal);

            document.add(totalsTable);
            document.add(new Paragraph(" "));

            // 5. NOTA LEGAL Y TÉRMINOS
            Paragraph termsTitle = new Paragraph("TÉRMINOS Y CONDICIONES DEL SERVICIO:", fontBold);
            Paragraph termsBody = new Paragraph(
                    "1. El depósito de garantía será reembolsado al finalizar el alquiler previa inspección de devolución y verificación del estado operativo.\n" +
                    "2. En caso de daños o faltantes, el valor correspondiente será debitado del depósito según el acta de inspección técnica.\n" +
                    "3. Este documento digital cuenta con validez legal y fiscal conforme a las resoluciones DIAN vigentes de facturación electrónica.",
                    fontFooter
            );
            document.add(termsTitle);
            document.add(termsBody);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new BusinessException("Error al generar documento PDF de la factura: " + e.getMessage());
        }
    }

    private void addTotalRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell l = new PdfPCell(new Phrase(label, font));
        l.setPadding(5);
        l.setBorderColor(new Color(226, 232, 240));
        table.addCell(l);

        PdfPCell v = new PdfPCell(new Phrase(value, font));
        v.setPadding(5);
        v.setHorizontalAlignment(Element.ALIGN_RIGHT);
        v.setBorderColor(new Color(226, 232, 240));
        table.addCell(v);
    }
}
