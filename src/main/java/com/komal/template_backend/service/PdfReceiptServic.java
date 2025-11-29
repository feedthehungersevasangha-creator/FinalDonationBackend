
// package com.komal.template_backend.service;

// import com.komal.template_backend.model.Donourentity;
// import org.apache.pdfbox.pdmodel.*;
// import org.apache.pdfbox.pdmodel.common.PDRectangle;
// import org.apache.pdfbox.pdmodel.font.PDType0Font;
// import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
// import org.springframework.stereotype.Service;

// import java.io.ByteArrayOutputStream;
// import java.io.InputStream;
// import java.time.format.DateTimeFormatter;

// @Service
// public class PdfReceiptServic {

//     private final String logoResource = "/logo.png";
//     private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

//     // -----------------------------
//     // UNIVERSAL UNICODE FONT LOADER
//     // -----------------------------
//     private PDType0Font loadUnicodeFont(PDDocument doc) throws Exception {
//         InputStream fontStream = getClass().getResourceAsStream("/NotoSans-Regular.ttf");
//         if (fontStream == null) {
//             throw new RuntimeException("Unicode font not found: /fonts/NotoSans-Regular.ttf");
//         }
//         return PDType0Font.load(doc, fontStream);
//     }

//     // -----------------------------
//     // 1. ONE-TIME DONATION RECEIPT
//     // -----------------------------
//     public byte[] generateOneTimeDonationReceipt(Donourentity d, String paymentId, double amount) throws Exception {

//         try (PDDocument doc = new PDDocument()) {

//             PDType0Font font = loadUnicodeFont(doc);

//             PDPage page = new PDPage(PDRectangle.A4);
//             doc.addPage(page);

//             PDPageContentStream cs = new PDPageContentStream(doc, page);
//             float margin = 40;
//             float y = page.getMediaBox().getHeight() - margin;

//             // Logo
//             InputStream logoStream = getClass().getResourceAsStream(logoResource);
//             if (logoStream != null) {
//                 PDImageXObject pdImage = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
//                 cs.drawImage(pdImage, margin, y - 80, 80, 80);
//             }

//             // Header
//             cs.beginText();
//             cs.setFont(font, 18);
//             cs.newLineAtOffset(margin + 100, y - 25);
//             cs.showText("Feed The Hunger Sewa Sangha");
//             cs.endText();

//             // Title
//             cs.beginText();
//             cs.setFont(font, 16);
//             cs.newLineAtOffset(margin, y - 120);
//             cs.showText("Donation Receipt");
//             cs.endText();

//             float startY = y - 160;
//             float leading = 18;

//             cs.beginText();
//             cs.setFont(font, 12);
//             cs.newLineAtOffset(margin, startY);

//             String emailForReceipt = (d.getPayerEmail() != null && !d.getPayerEmail().isEmpty())
//                     ? d.getPayerEmail() : d.getEmail();

//             cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//             cs.newLineAtOffset(0, -leading);
//             cs.showText("Email: " + safe(emailForReceipt));
//             cs.newLineAtOffset(0, -leading);
//             cs.showText("Mobile: " + safe(d.getMobile()));
//             cs.newLineAtOffset(0, -leading);
//             cs.showText("Amount: ₹" + String.format("%.2f", amount)); // NOW SUPPORTED
//             cs.newLineAtOffset(0, -leading);
//             cs.showText("Payment ID: " + (paymentId == null ? "-" : paymentId));
//             cs.newLineAtOffset(0, -leading);
//             cs.showText("Order ID: " + safe(d.getOrderId()));
//             cs.newLineAtOffset(0, -leading);
//             cs.showText("Date: " + (d.getDonationDate() != null
//                     ? dtf.format(d.getDonationDate())
//                     : java.time.LocalDateTime.now().format(dtf)));

//             cs.endText();

//             // Footer
//             cs.beginText();
//             cs.setFont(font, 10);
//             cs.newLineAtOffset(margin, 120);
//             cs.showText("This is a system generated receipt and does not require a signature.");
//             cs.endText();

//             cs.close();

//             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             doc.save(baos);
//             return baos.toByteArray();
//         }
//     }

//     // -----------------------------
//     // 2. MANDATE CONFIRMATION
//     // -----------------------------
//     public byte[] generateMandateConfirmation(Donourentity d) throws Exception {

//         try (PDDocument doc = new PDDocument()) {

//             PDType0Font font = loadUnicodeFont(doc);

//             PDPage page = new PDPage(PDRectangle.A4);
//             doc.addPage(page);

//             PDPageContentStream cs = new PDPageContentStream(doc, page);
//             float margin = 40;
//             float y = page.getMediaBox().getHeight() - margin;

//             InputStream logoStream = getClass().getResourceAsStream(logoResource);
//             if (logoStream != null) {
//                 PDImageXObject img = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
//                 cs.drawImage(img, margin, y - 80, 80, 80);
//             }

//             cs.beginText();
//             cs.setFont(font, 16);
//             cs.newLineAtOffset(margin, y - 120);
//             cs.showText("Mandate Registration Confirmation");
//             cs.endText();

//             cs.beginText();
//             cs.setFont(font, 12);
//             cs.newLineAtOffset(margin, y - 170);

//             cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//             cs.newLineAtOffset(0, -14);
//             cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
//             cs.newLineAtOffset(0, -14);
//             cs.showText("Mandate ID: " + safe(d.getMandateId()));
//             cs.newLineAtOffset(0, -14);
//             cs.showText("Mandate Status: " + safe(d.getMandateStatus()));
//             cs.newLineAtOffset(0, -14);
//             cs.showText("Monthly Amount: ₹" +
//                     (d.getMonthlyAmount() == null ? "-" : String.format("%.2f", d.getMonthlyAmount())));
//             cs.endText();

//             cs.beginText();
//             cs.setFont(font, 10);
//             cs.newLineAtOffset(margin, 120);
//             cs.showText("This confirms registration of your e-mandate. Monthly receipts will be sent after debits.");
//             cs.endText();

//             cs.close();

//             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             doc.save(baos);
//             return baos.toByteArray();
//         }
//     }

//     // -----------------------------
//     // 3. MONTHLY DEBIT RECEIPT
//     // -----------------------------
//     public byte[] generateMonthlyDebitReceipt(Donourentity d, String paymentId, double amount) throws Exception {

//         try (PDDocument doc = new PDDocument()) {

//             PDType0Font font = loadUnicodeFont(doc);

//             PDPage page = new PDPage(PDRectangle.A4);
//             doc.addPage(page);

//             PDPageContentStream cs = new PDPageContentStream(doc, page);
//             float margin = 40;
//             float y = page.getMediaBox().getHeight() - margin;

//             InputStream logoStream = getClass().getResourceAsStream(logoResource);
//             if (logoStream != null) {
//                 PDImageXObject img = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
//                 cs.drawImage(img, margin, y - 80, 80, 80);
//             }

//             cs.beginText();
//             cs.setFont(font, 16);
//             cs.newLineAtOffset(margin, y - 120);
//             cs.showText("Monthly Donation Receipt");
//             cs.endText();

//             cs.beginText();
//             cs.setFont(font, 12);
//             cs.newLineAtOffset(margin, y - 165);

//             cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//             cs.newLineAtOffset(0, -14);
//             cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
//             cs.newLineAtOffset(0, -14);
//             cs.showText("Payment ID: " + safe(paymentId));
//             cs.newLineAtOffset(0, -14);
//             cs.showText("Amount: ₹" + String.format("%.2f", amount));
//             cs.newLineAtOffset(0, -14);
//             cs.showText("Date: " + (d.getDonationDate() != null
//                     ? dtf.format(d.getDonationDate())
//                     : java.time.LocalDateTime.now().format(dtf)));

//             cs.endText();

//             cs.beginText();
//             cs.setFont(font, 10);
//             cs.newLineAtOffset(margin, 120);
//             cs.showText("Thank you for your continued support.");
//             cs.endText();

//             cs.close();

//             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             doc.save(baos);
//             return baos.toByteArray();
//         }
//     }

//     private String safe(String v) {
//         return v == null ? "-" : v;
//     }
// }
// -------------------------------------------------------------------------------------------------------------
// package com.komal.template_backend.service;

// import com.komal.template_backend.model.Donourentity;
// import org.apache.pdfbox.pdmodel.*;
// import org.apache.pdfbox.pdmodel.common.PDRectangle;
// import org.apache.pdfbox.pdmodel.font.PDType0Font;
// import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
// import org.springframework.stereotype.Service;

// import java.io.ByteArrayOutputStream;
// import java.io.InputStream;
// import java.time.LocalDate;
// import java.time.format.DateTimeFormatter;

// @Service
// public class PdfReceiptServic {

//     private final String logoResource = "/logo.png";
//     private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

//     // ----------- FONT LOADER -----------
//     private PDType0Font loadUnicodeFont(PDDocument doc) throws Exception {
//         InputStream fontStream = getClass().getResourceAsStream("/NotoSans-Regular.ttf");
//         if (fontStream == null) {
//             throw new RuntimeException("Unicode font missing: NotoSans-Regular.ttf");
//         }
//         return PDType0Font.load(doc, fontStream);
//     }

//     // ----------- HEADER BLOCK (used by all receipts) -----------
//     private float drawHeader(PDDocument doc, PDPageContentStream cs, PDType0Font font, float y) throws Exception {

//         float margin = 40;

//         // Logo
//         InputStream logoStream = getClass().getResourceAsStream(logoResource);
//         if (logoStream != null) {
//             PDImageXObject logo = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
//             cs.drawImage(logo, margin, y - 80, 80, 80);
//         }

//         // Org Name
//         cs.beginText();
//         cs.setFont(font, 16);
//         cs.newLineAtOffset(margin + 100, y - 25);
//         cs.showText("Feed The Hunger Seva Sangha R");
//         cs.endText();

//         // Address
//         cs.beginText();
//         cs.setFont(font, 10);
//         cs.newLineAtOffset(margin + 100, y - 45);
//         cs.showText("SARJAPURA VILLAGE, SARJAPURA POST, BENGALURU, KARNATAKA - 562125");
//         cs.newLineAtOffset(0, -14);
//         cs.showText("Phone: 918884260100   Email: feedthehunger.india2025@gmail.com");
//         cs.endText();

//         return y - 120;
//     }

//     // ----------- FOOTER BOX -----------
//     private void drawFooterBox(PDPageContentStream cs, PDType0Font font, float margin, PDPage page) throws Exception {

//         float boxY = 90;
//         float boxHeight = 60;

//         cs.addRect(margin, boxY, page.getMediaBox().getWidth() - margin * 2, boxHeight);
//         cs.stroke();

//         cs.beginText();
//         cs.setFont(font, 9);
//         cs.newLineAtOffset(margin + 10, boxY + boxHeight - 20);

//         cs.showText("Registered as Public Charitable Trust No. 616. Donations made to the Trust");
//         cs.newLineAtOffset(0, -12);
//         cs.showText("are eligible for exemption under section 80G of the Income Tax Act 1961.");
//         cs.newLineAtOffset(0, -12);
//         cs.showText("PAN: AAATG3353R");

//         cs.endText();
//     }

//     // ================================================================
//     // 1) ONE-TIME DONATION RECEIPT (GREENPEACE FORMAT)
//     // ================================================================
//     public byte[] generateOneTimeDonationReceipt(Donourentity d, String paymentId, double amount) throws Exception {

//         try (PDDocument doc = new PDDocument()) {

//             PDType0Font font = loadUnicodeFont(doc);
//             PDPage page = new PDPage(PDRectangle.A4);
//             doc.addPage(page);

//             PDPageContentStream cs = new PDPageContentStream(doc, page);
//             float margin = 40;

//             // HEADER
//             float y = drawHeader(doc, cs, font, page.getMediaBox().getHeight() - margin);

//             // RECEIPT INFO
//             cs.beginText();
//             cs.setFont(font, 12);
//             cs.newLineAtOffset(margin, y);

//             cs.showText("Receipt No: " + safe(d.getReceiptNo()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Financial Year: 2024-25");
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Date of Print: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

//             cs.endText();

//             // "TO" BLOCK
//             y -= 80;

//             cs.beginText();
//             cs.setFont(font, 12);
//             cs.newLineAtOffset(margin, y);

//             cs.showText("To");
//             cs.newLineAtOffset(0, -18);
//             cs.showText(safe(d.getFirstName()) + " " + safe(d.getLastName()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText(safe(d.getAddress()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Phone: " + safe(d.getMobile()));

//             cs.endText();

//             // DONATION DETAILS
//             y -= 110;

//             cs.beginText();
//             cs.setFont(font, 12);
//             cs.newLineAtOffset(margin, y);

//             cs.showText("Donor ID: " + safe(d.getDonorId()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Received with Thanks from: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("A Sum of Rupees: INR " + String.format("%.2f", amount) + "/-");
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Payment ID: " + safe(paymentId));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Order ID: " + safe(d.getOrderId()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Date: " + dtf.format(d.getDonationDate()));

//             cs.endText();

//             // FOOTER
//             drawFooterBox(cs, font, margin, page);

//             cs.close();

//             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             doc.save(baos);
//             return baos.toByteArray();
//         }
//     }

//     // ================================================================
//     // 2) MANDATE REGISTRATION RECEIPT (GREENPEACE FORMAT)
//     // ================================================================
//     public byte[] generateMandateConfirmation(Donourentity d) throws Exception {

//         try (PDDocument doc = new PDDocument()) {

//             PDType0Font font = loadUnicodeFont(doc);
//             PDPage page = new PDPage(PDRectangle.A4);
//             doc.addPage(page);

//             PDPageContentStream cs = new PDPageContentStream(doc, page);
//             float margin = 40;

//             // HEADER
//             float y = drawHeader(doc, cs, font, page.getMediaBox().getHeight() - margin);

//             // TITLE BLOCK
//             cs.beginText();
//             cs.setFont(font, 16);
//             cs.newLineAtOffset(margin, y - 10);
//             cs.showText("Mandate Registration Confirmation");
//             cs.endText();

//             y -= 70;

//             // DETAILS
//             cs.beginText();
//             cs.setFont(font, 12);
//             cs.newLineAtOffset(margin, y);

//             cs.showText("Donor ID: " + safe(d.getDonorId()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Mandate ID: " + safe(d.getMandateId()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Mandate Status: " + safe(d.getMandateStatus()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Monthly Amount: INR " + safe(String.valueOf(d.getMonthlyAmount())));

//             cs.endText();

//             // FOOTER
//             drawFooterBox(cs, font, margin, page);

//             cs.close();

//             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             doc.save(baos);
//             return baos.toByteArray();
//         }
//     }

//     // ================================================================
//     // 3) MONTHLY DEBIT RECEIPT (GREENPEACE FORMAT)
//     // ================================================================
//     public byte[] generateMonthlyDebitReceipt(Donourentity d, String paymentId, double amount) throws Exception {

//         try (PDDocument doc = new PDDocument()) {

//             PDType0Font font = loadUnicodeFont(doc);
//             PDPage page = new PDPage(PDRectangle.A4);
//             doc.addPage(page);

//             PDPageContentStream cs = new PDPageContentStream(doc, page);
//             float margin = 40;

//             // HEADER
//             float y = drawHeader(doc, cs, font, page.getMediaBox().getHeight() - margin);

//             // TITLE
//             cs.beginText();
//             cs.setFont(font, 16);
//             cs.newLineAtOffset(margin, y - 10);
//             cs.showText("Monthly Donation Receipt");
//             cs.endText();

//             y -= 70;

//             // DETAILS
//             cs.beginText();
//             cs.setFont(font, 12);
//             cs.newLineAtOffset(margin, y);

//             cs.showText("Donor ID: " + safe(d.getDonorId()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Payment ID: " + safe(paymentId));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Amount Debited: INR " + String.format("%.2f", amount));
//             cs.newLineAtOffset(0, -16);
//             cs.showText("Date: " + dtf.format(d.getDonationDate()));

//             cs.endText();

//             // FOOTER
//             drawFooterBox(cs, font, margin, page);

//             cs.close();

//             ByteArrayOutputStream baos = new ByteArrayOutputStream();
//             doc.save(baos);
//             return baos.toByteArray();
//         }
//     }

//     private String safe(String v) {
//         return v == null ? "-" : v;
//     }
// }
// -----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
package com.komal.template_backend.service;

import com.komal.template_backend.model.Donourentity;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReceiptServic {

    private final String logoResource = "/logo.png";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    // -----------------------------
    // FONT LOADER
    // -----------------------------
    private PDType0Font loadUnicodeFont(PDDocument doc) throws Exception {
        InputStream fontStream = getClass().getResourceAsStream("/NotoSans-Regular.ttf");
        if (fontStream == null) {
            throw new RuntimeException("Unicode font missing: NotoSans-Regular.ttf");
        }
        return PDType0Font.load(doc, fontStream);
    }

    // -----------------------------
    // HEADER (same for all receipts)
    // -----------------------------
    private float drawHeader(PDDocument doc, PDPageContentStream cs, PDType0Font font, float y) throws Exception {

        float margin = 40;

        // Logo
        InputStream logoStream = getClass().getResourceAsStream(logoResource);
        if (logoStream != null) {
            PDImageXObject logo = PDImageXObject.createFromByteArray(doc, logoStream.readAllBytes(), "logo");
            cs.drawImage(logo, margin, y - 80, 80, 80);
        }

        // Org Name
        cs.beginText();
        cs.setFont(font, 16);
        cs.newLineAtOffset(margin + 100, y - 25);
        cs.showText("Feed The Hunger Seva Sangha R");
        cs.endText();

        // Address + Contact
        cs.beginText();
        cs.setFont(font, 10);
        cs.newLineAtOffset(margin + 100, y - 45);
        cs.showText("SARJAPURA VILLAGE, SARJAPURA POST, BENGALURU, KARNATAKA - 562125");
        cs.newLineAtOffset(0, -14);
        cs.showText("Phone: 918884260100   Email: feedthehunger.india2025@gmail.com");
        cs.endText();

        return y - 120; // next y position
    }

    // -----------------------------
    // FOOTER BOX
    // -----------------------------
    private void drawFooterBox(PDPageContentStream cs, PDType0Font font, float margin, PDPage page) throws Exception {

        float boxY = 90;
        float boxHeight = 60;

        cs.addRect(margin, boxY, page.getMediaBox().getWidth() - margin * 2, boxHeight);
        cs.stroke();

        cs.beginText();
        cs.setFont(font, 9);
        cs.newLineAtOffset(margin + 10, boxY + boxHeight - 20);

        cs.showText("Registered as Public Charitable Trust No. 616. Donations made to the Trust");
        cs.newLineAtOffset(0, -12);
        cs.showText("are eligible for exemption under section 80G of the Income Tax Act 1961.");
        cs.newLineAtOffset(0, -12);
        cs.showText("PAN: AAATG3353R");

        cs.endText();
    }

    // =========================================
    // 1) ONE-TIME DONATION RECEIPT
    // =========================================
    public byte[] generateOneTimeDonationReceipt(Donourentity d, String paymentId, double amount) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDType0Font font = loadUnicodeFont(doc);
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 40;

            float y = drawHeader(doc, cs, font, page.getMediaBox().getHeight() - margin);

            // Receipt Info
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);

            cs.showText("Receipt No: " + safe(d.getInvoiceNumber()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Financial Year: 2024-25");
            cs.newLineAtOffset(0, -16);
            cs.showText("Date of Print: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));

            cs.endText();

            // TO Block
            y -= 80;

            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);

            cs.showText("To");
            cs.newLineAtOffset(0, -18);
            cs.showText(safe(d.getFirstName()) + " " + safe(d.getLastName()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Phone: " + safe(d.getMobile()));

            cs.endText();

            // Donation Details
            y -= 110;

            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);

            cs.showText("Donor ID: " + safe(d.getId()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Received with Thanks from: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Amount: INR " + String.format("%.2f", amount) + "/-");
            cs.newLineAtOffset(0, -16);
            cs.showText("Payment ID: " + safe(paymentId));
            cs.newLineAtOffset(0, -16);
            cs.showText("Order ID: " + safe(d.getOrderId()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Date: " + dtf.format(d.getDonationDate()));

            cs.endText();

            // Footer
            drawFooterBox(cs, font, margin, page);

            cs.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // =========================================
    // 2) MANDATE CONFIRMATION RECEIPT
    // =========================================
    public byte[] generateMandateConfirmation(Donourentity d) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDType0Font font = loadUnicodeFont(doc);
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 40;

            float y = drawHeader(doc, cs, font, page.getMediaBox().getHeight() - margin);

            // Title
            cs.beginText();
            cs.setFont(font, 16);
            cs.newLineAtOffset(margin, y - 10);
            cs.showText("Mandate Registration Confirmation");
            cs.endText();

            y -= 70;

            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);

            cs.showText("Donor ID: " + safe(d.getId()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Mandate ID: " + safe(d.getMandateId()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Mandate Status: " + safe(d.getMandateStatus()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Monthly Amount: INR " + safe(String.valueOf(d.getMonthlyAmount())));

            cs.endText();

            drawFooterBox(cs, font, margin, page);

            cs.close();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    // =========================================
    // 3) MONTHLY DEBIT RECEIPT
    // =========================================
    public byte[] generateMonthlyDebitReceipt(Donourentity d, String paymentId, double amount) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            PDType0Font font = loadUnicodeFont(doc);
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float margin = 40;

            float y = drawHeader(doc, cs, font, page.getMediaBox().getHeight() - margin);

            // Title
            cs.beginText();
            cs.setFont(font, 16);
            cs.newLineAtOffset(margin, y - 10);
            cs.showText("Monthly Donation Receipt");
            cs.endText();

            y -= 70;

            // Details
            cs.beginText();
            cs.setFont(font, 12);
            cs.newLineAtOffset(margin, y);

            cs.showText("Donor ID: " + safe(d.getId()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Donor: " + safe(d.getFirstName()) + " " + safe(d.getLastName()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Subscription ID: " + safe(d.getSubscriptionId()));
            cs.newLineAtOffset(0, -16);
            cs.showText("Payment ID: " + safe(paymentId));
            cs.newLineAtOffset(0, -16);
            cs.showText("Amount Debited: INR " + String.format("%.2f", amount));
            cs.newLineAtOffset(0, -16);
            cs.showText("Date: " + dtf.format(d.getDonationDate()));

            cs.endText();

            drawFooterBox(cs, font, margin, page);

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


