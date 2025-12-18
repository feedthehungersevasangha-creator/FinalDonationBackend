
package com.komal.template_backend.service;

import com.komal.template_backend.model.Donourentity;
import com.komal.template_backend.repo.DonationRepo;
import com.komal.template_backend.service.MailService;
import com.komal.template_backend.service.PdfReceiptServic;
import com.komal.template_backend.service.DonationService;

import com.razorpay.RazorpayClient;
import com.razorpay.Subscription;

import org.json.JSONObject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
@Scheduled(fixedDelay = 10 * 60 * 1000) // every 15 mins
public void syncPureMandates() {

    List<Donourentity> donors =
            donationRepo.findPendingPureMandates(); // status != CONFIRMED

    if (donors.isEmpty()) return;

    try {
        RazorpayClient client = new RazorpayClient(keyId, keySecret);

        for (Donourentity donor : donors) {

            if (donor.getPaymentId() == null) continue;

            Payment payment =
                    client.payments.fetch(donor.getPaymentId());

            JSONObject token = payment.toJson().optJSONObject("token");
            if (token == null) continue;

            String status = token.optString("status", "").toUpperCase();

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
        e.printStackTrace();
    }
}
