//package com.komal.template_backend.service;
//
//import com.komal.template_backend.model.Donourentity;
//import org.apache.pdfbox.pdmodel.*;
//import org.apache.pdfbox.pdmodel.common.PDRectangle;
//import org.apache.pdfbox.pdmodel.font.PDType1Font;
//import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
//import org.springframework.stereotype.Service;
//
//
//import java.io.ByteArrayOutputStream;
//import java.io.InputStream;
//import java.time.format.DateTimeFormatter;
//@Service
//public class PdfReceiptServic {
//
//    // Use resource path or absolute path if necessary
//    private final String logoResource = "/logo.png"; // put logo in src/main/resources/logo.png
//    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
//
//    public byte[] generateOneTimeDonationReceipt(Donourentity d, String paymentId, double amount) throws Exception {
//        try (PDDocument doc = new PDDocument()) {
//            PDPage page = new PDPage(PDRectangle.A4);
//            doc.addPage(page);
//
//            PDPageContentStream cs = new PDPageContentStream(doc, page);
//            float margin = 40;
//            float y = page.getMediaBox().getHeight() - margin;
//
//            // Logo
//            InputStream logoStream = getClass().getResourceAsStream(logoResource);
//            if (logoStream != null) {
//                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
//                float logoW = 80, logoH = 80;
//                cs.drawImage(pdImage, margin, y - logoH, logoW, logoH);
//            }
//
//            // Header
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA_BOLD, 18);
//            cs.newLineAtOffset(margin + 100, y - 25);
//            cs.showText("Feed The Hunger Sewa Sangha");
//            cs.endText();
//
//            // Title
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
//            cs.newLineAtOffset(margin, y - 120);
//            cs.showText("Donation Receipt");
//            cs.endText();
//
//            // Donor & Payment details (table-like)
//            float startY = y - 150;
//            float leading = 16;
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA, 11);
//            cs.newLineAtOffset(margin, startY);
//
//            cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//            cs.newLineAtOffset(0, -leading);
////            cs.showText("Email: " + safe(d.getPayerEmailOrDecrypted())); // placeholder - fill later
//            String emailForReceipt = (d.getPayerEmail() != null && !d.getPayerEmail().isEmpty())
//                    ? d.getPayerEmail()
//                    : d.getEmail();
//
//
//
//            cs.showText("Email: " + safe(emailForReceipt));
//
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Mobile: " + safe(d.getMobile()));
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Amount: ₹" + String.format("%.2f", amount));
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Payment ID: " + (paymentId == null ? "-" : paymentId));
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Order ID: " + safe(d.getOrderId()));
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Date: " + (d.getDonationDate() != null ? dtf.format(d.getDonationDate()) : java.time.LocalDateTime.now().format(dtf)));
//            cs.endText();
//
//            // Footer & Authorized sign
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
//            cs.newLineAtOffset(margin, 120);
//            cs.showText("This is a system generated receipt and does not require a signature.");
//            cs.endText();
//
//            cs.close();
//
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            doc.save(baos);
//            return baos.toByteArray();
//        }
//    }
//
//    public byte[] generateMandateConfirmation(Donourentity d) throws Exception {
//        try (PDDocument doc = new PDDocument()) {
//            PDPage page = new PDPage(PDRectangle.A4);
//            doc.addPage(page);
//            PDPageContentStream cs = new PDPageContentStream(doc, page);
//
//            float margin = 40;
//            float y = page.getMediaBox().getHeight() - margin;
//
//            InputStream logoStream = getClass().getResourceAsStream(logoResource);
//            if (logoStream != null) {
//                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
//                cs.drawImage(pdImage, margin, y - 80, 80, 80);
//            }
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
//            cs.newLineAtOffset(margin, y - 120);
//            cs.showText("Mandate Registration Confirmation");
//            cs.endText();
//
//            float startY = y - 160;
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA, 11);
//            cs.newLineAtOffset(margin, startY);
//            cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//            cs.newLineAtOffset(0, -14);
//            cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
//            cs.newLineAtOffset(0, -14);
//            cs.showText("Mandate ID: " + safe(d.getMandateId()));
//            cs.newLineAtOffset(0, -14);
//            cs.showText("Mandate Status: " + safe(d.getMandateStatus()));
//            cs.newLineAtOffset(0, -14);
//            cs.showText("Monthly Amount: ₹" + (d.getMonthlyAmount() == null ? "-" : String.format("%.2f", d.getMonthlyAmount())));
//            cs.endText();
//
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
//            cs.newLineAtOffset(margin, 120);
//            cs.showText("This confirms registration of your e-mandate. Monthly receipts will be sent after debits.");
//            cs.endText();
//
//            cs.close();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            doc.save(baos);
//            return baos.toByteArray();
//        }
//    }
//
//    public byte[] generateMonthlyDebitReceipt(Donourentity d, String paymentId, double amount) throws Exception {
//        // very similar to one-time but mention subscription details
//        try (PDDocument doc = new PDDocument()) {
//            PDPage page = new PDPage(PDRectangle.A4);
//            doc.addPage(page);
//            PDPageContentStream cs = new PDPageContentStream(doc, page);
//            float margin = 40;
//            float y = page.getMediaBox().getHeight() - margin;
//            InputStream logoStream = getClass().getResourceAsStream(logoResource);
//            if (logoStream != null) {
//                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
//                cs.drawImage(pdImage, margin, y - 80, 80, 80);
//            }
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
//            cs.newLineAtOffset(margin, y - 120);
//            cs.showText("Monthly Donation Receipt");
//            cs.endText();
//            float startY = y - 150;
//            float leading = 14;
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA, 11);
//            cs.newLineAtOffset(margin, startY);
//            cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Payment ID: " + paymentId);
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Amount: ₹" + String.format("%.2f", amount));
//            cs.newLineAtOffset(0, -leading);
//            cs.showText("Date: " + (d.getDonationDate() != null ? dtf.format(d.getDonationDate()) : java.time.LocalDateTime.now().format(dtf)));
//            cs.endText();
//            cs.beginText();
//            cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
//            cs.newLineAtOffset(margin, 120);
//            cs.showText("Thank you for your continued support.");
//            cs.endText();
//            cs.close();
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            doc.save(baos);
//            return baos.toByteArray();
//        }
//    }
//
//    private String safe(String v) {
//        return v == null ? "-" : v;
//    }
//}
package com.komal.template_backend.service;

import com.komal.template_backend.model.Donourentity;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReceiptServic {

    private final String logoResource = "/logo.png";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // -----------------------------
    // UNIVERSAL UNICODE FONT LOADER
    // -----------------------------
    private PDType0Font loadUnicodeFont(PDDocument doc) throws Exception {
        InputStream fontStream = getClass().getResourceAsStream("/NotoSans-Regular.ttf");
        if (fontStream == null) {
            throw new RuntimeException("Unicode font not found: /fonts/NotoSans-Regular.ttf");
        }
        return PDType0Font.load(doc, fontStream);
    }

    // -----------------------------
    // 1. ONE-TIME DONATION RECEIPT
    // -----------------------------
    public byte[] generateOneTimeDonationReceipt(Donourentity d, String paymentId, double amount) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDType0Font font = loadUnicodeFont(doc);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 40;
            float y = page.getMediaBox().getHeight() - margin;

            // Logo
            InputStream logoStream = getClass().getResourceAsStream(logoResource);
            if (logoStream != null) {
                PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
                cs.drawImage(pdImage, margin, y - 80, 80, 80);
            }

            // Header
            cs.beginText();
            cs.setFont(font, 18);
            cs.newLineAtOffset(margin + 100, y - 25);
            cs.showText("Feed The Hunger Sewa Sangha");
            cs.endText();

            // Title
            cs.beginText();
            cs.setFont(font, 16);
            cs.newLineAtOffset(margin, y - 120);
            cs.showText("Donation Receipt");
            cs.endText();

            float startY = y - 160;
            float leading = 18;

            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, startY);

            String emailForReceipt = (d.getPayerEmail() != null && !d.getPayerEmail().isEmpty())
                    ? d.getPayerEmail() : d.getEmail();

            cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
            cs.newLineAtOffset(0, -leading);
            cs.showText("Email: " + safe(emailForReceipt));
            cs.newLineAtOffset(0, -leading);
            cs.showText("Mobile: " + safe(d.getMobile()));
            cs.newLineAtOffset(0, -leading);
            cs.showText("Amount: ₹" + String.format("%.2f", amount)); // NOW SUPPORTED
            cs.newLineAtOffset(0, -leading);
            cs.showText("Payment ID: " + (paymentId == null ? "-" : paymentId));
            cs.newLineAtOffset(0, -leading);
            cs.showText("Order ID: " + safe(d.getOrderId()));
            cs.newLineAtOffset(0, -leading);
            cs.showText("Date: " + (d.getDonationDate() != null
                    ? dtf.format(d.getDonationDate())
                    : java.time.LocalDateTime.now().format(dtf)));

            cs.endText();

            // Footer
            cs.beginText();
            cs.setFont(font, 10);
            cs.newLineAtOffset(margin, 120);
            cs.showText("This is a system generated receipt and does not require a signature.");
            cs.endText();

            cs.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // -----------------------------
    // 2. MANDATE CONFIRMATION
    // -----------------------------
    public byte[] generateMandateConfirmation(Donourentity d) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDType0Font font = loadUnicodeFont(doc);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 40;
            float y = page.getMediaBox().getHeight() - margin;

            InputStream logoStream = getClass().getResourceAsStream(logoResource);
            if (logoStream != null) {
                PDImageXObject img = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
                cs.drawImage(img, margin, y - 80, 80, 80);
            }

            cs.beginText();
            cs.setFont(font, 16);
            cs.newLineAtOffset(margin, y - 120);
            cs.showText("Mandate Registration Confirmation");
            cs.endText();

            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y - 170);

            cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
            cs.newLineAtOffset(0, -14);
            cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
            cs.newLineAtOffset(0, -14);
            cs.showText("Mandate ID: " + safe(d.getMandateId()));
            cs.newLineAtOffset(0, -14);
            cs.showText("Mandate Status: " + safe(d.getMandateStatus()));
            cs.newLineAtOffset(0, -14);
            cs.showText("Monthly Amount: ₹" +
                    (d.getMonthlyAmount() == null ? "-" : String.format("%.2f", d.getMonthlyAmount())));
            cs.endText();

            cs.beginText();
            cs.setFont(font, 10);
            cs.newLineAtOffset(margin, 120);
            cs.showText("This confirms registration of your e-mandate. Monthly receipts will be sent after debits.");
            cs.endText();

            cs.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // -----------------------------
    // 3. MONTHLY DEBIT RECEIPT
    // -----------------------------
    public byte[] generateMonthlyDebitReceipt(Donourentity d, String paymentId, double amount) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDType0Font font = loadUnicodeFont(doc);

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 40;
            float y = page.getMediaBox().getHeight() - margin;

            InputStream logoStream = getClass().getResourceAsStream(logoResource);
            if (logoStream != null) {
                PDImageXObject img = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
                cs.drawImage(img, margin, y - 80, 80, 80);
            }

            cs.beginText();
            cs.setFont(font, 16);
            cs.newLineAtOffset(margin, y - 120);
            cs.showText("Monthly Donation Receipt");
            cs.endText();

            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y - 165);

            cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
            cs.newLineAtOffset(0, -14);
            cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
            cs.newLineAtOffset(0, -14);
            cs.showText("Payment ID: " + safe(paymentId));
            cs.newLineAtOffset(0, -14);
            cs.showText("Amount: ₹" + String.format("%.2f", amount));
            cs.newLineAtOffset(0, -14);
            cs.showText("Date: " + (d.getDonationDate() != null
                    ? dtf.format(d.getDonationDate())
                    : java.time.LocalDateTime.now().format(dtf)));

            cs.endText();

            cs.beginText();
            cs.setFont(font, 10);
            cs.newLineAtOffset(margin, 120);
            cs.showText("Thank you for your continued support.");
            cs.endText();

            cs.close();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private String safe(String v) {
        return v == null ? "-" : v;
    }
}
