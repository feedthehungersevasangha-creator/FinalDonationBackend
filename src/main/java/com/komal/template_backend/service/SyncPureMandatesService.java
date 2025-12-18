package com.komal.template_backend.service;

import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;

import com.razorpay.RazorpayClient;
import com.razorpay.Payment;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SyncPureMandatesService {

    @Value("${razorpay.key_id}")
    private String keyId;

    @Value("${razorpay.key_secret}")
    private String keySecret;

    private final DonationRepo donationRepo;
    private final MailService mailService;
    private final PdfReceiptServic pdfReceiptService;
    private final DonationService donationService;

    public SyncPureMandatesService(
            DonationRepo donationRepo,
            MailService mailService,
            PdfReceiptServic pdfReceiptService,
            DonationService donationService
    ) {
        this.donationRepo = donationRepo;
        this.mailService = mailService;
        this.pdfReceiptService = pdfReceiptService;
        this.donationService = donationService;
    }

    // 🔁 Runs every 10 minutes
    @Scheduled(fixedDelay = 10 * 60 * 1000)
    public void syncPureMandates() {

        List<Donourentity> donors =
                donationRepo.findPendingPureMandates();

        if (donors.isEmpty()) {
            System.out.println("🔄 Pure mandate sync: nothing to process");
            return;
        }

        System.out.println("🔄 Pure mandate sync started | count=" + donors.size());

        try {
            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            for (Donourentity donor : donors) {

                if (donor.getPaymentId() == null) continue;

                Payment payment =
                        client.payments.fetch(donor.getPaymentId());

                JSONObject token =
                        payment.toJson().optJSONObject("token");

                if (token == null) continue;

                String status =
                        token.optString("status", "").toUpperCase();

                System.out.println(
                        "🔍 Pure Mandate check | donor=" + donor.getId()
                      + " status=" + status
                );

                if ("CONFIRMED".equals(status)) {

                    donor.setMandateStatus("AUTHORIZED");
                    donor.setStatus("SUCCESS");

                    if (Boolean.FALSE.equals(donor.getMandateMailSent())) {

                        Donourentity decrypted =
                                donationService.findByIdDecrypt(donor.getId());

                        byte[] pdf =
                                pdfReceiptService.generateMandateConfirmation(decrypted);

                        mailService.sendDonationReceiptWithAttachment(
                                decrypted.getEmail(),
                                decrypted.getFirstName() + " " + decrypted.getLastName(),
                                donor.getMandateAmount(),
                                donor.getMandateId(),
                                pdf,
                                "Mandate_Approved_" + donor.getMandateId() + ".pdf"
                        );

                        donor.setMandateMailSent(true);
                    }

                    donationRepo.save(donor);
                }
            }

        } catch (Exception e) {
            System.out.println("❌ Pure mandate sync failed");
            e.printStackTrace();
        }
    }
}
